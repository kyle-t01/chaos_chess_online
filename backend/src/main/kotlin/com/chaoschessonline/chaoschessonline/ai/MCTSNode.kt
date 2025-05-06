package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.model.BoardState
import kotlin.math.sqrt

/**
 * MONTE CARLO TREE SEARCH NODE for MCTS
 *
 * @constructor Create empty MCTS root node
 */
data class MCTSNode (val parent: MCTSNode?, val state: BoardState, val children: MutableList<MCTSNode>, val untriedStates: MutableList<BoardState>)
{
    var wins = 0.0
    var visits = 0.0




    fun select(): MCTSNode {
        val root = this
        var curr = root
        // find leaf node
        // while tried every state of this node, and have a next child
        while(curr.untriedStates.isEmpty() && curr.children.isNotEmpty()) {
            // select best child
            curr = curr.children.maxBy { it.uct() }
        }
        return curr
    }

    fun expand(): MCTSNode {

        // add/expand children
        // if tried all next states already return ref
        if (untriedStates.isEmpty()) return this

        // otherwise, use A untriedState to make a new child node
        val nextState = untriedStates.removeLast()
        val newUntriedStates = nextState.generateNextStates().toMutableList()

        // make a new MCTS node
        val child = MCTSNode(this, nextState, mutableListOf(), newUntriedStates)
        children.add(child)
        return child
    }

    fun rollout(root: MCTSNode): Double {
        val terminalState = NextStateMaker.playRandomlyTilTerminal(state)
        val terminalEval = StateEvaluator.findTacticalScore(terminalState)
        val loserPlayer = terminalState.attackingDirection
        val rootPlayer = root.state.attackingDirection
        // is terminal state caused by rootPlayer?
        //println("terminalState caused by root player: ${terminalPlayer != rootPlayer}, $score, $terminalScore")
        // get perspective of root
        if (!StateEvaluator.scoreIsTerminal(terminalEval)) {
            // if not terminal score, then likely a draw so not a win
            println(">>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<")
            println("got this termScore instead $terminalEval")
            println("the board was ${terminalState.board}")
            println(">>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<")
            return 0.0
        }

        val distanceToRoot = terminalState.turnNumber - root.state.turnNumber
        val bestEvalPlayer = StateEvaluator.bestEvalOfPlayer(rootPlayer)
        if (bestEvalPlayer == terminalEval) {
            // TODO: check whether it is an instant win, if so, do something with it
            // for now, return whether we have won
            return 1.0
        }
        if (distanceToRoot <= 2) {
            return Double.NEGATIVE_INFINITY
        }

        return 0.0
    }

    fun backPropagate(result: Double) {
        var curr: MCTSNode? = this
        while (curr != null) {
            curr.visits += 1.0
            curr.wins += result
            curr = curr.parent
        }
    }

    fun uct(): Double {
        // uct score is always +ve regardless the player at the state
        if (visits == 0.0) return Double.POSITIVE_INFINITY
        val N = parent?.visits ?: 1
        val n = visits
        val w = wins
        val logN = Math.log(N.toDouble())
        val exploit = (1.0)*w/n
        val explore = EXPLORATION_PARAM * sqrt(logN/n)
        return exploit + explore
    }

    fun getBestChild(): MCTSNode {
        // else next moves are losing or non-losing
        val nonLosing = children.filter{it.wins != Double.NEGATIVE_INFINITY}
        if (nonLosing.isEmpty()) return this
        return nonLosing.maxBy{ it.visits}
    }

    fun toState(): BoardState {
        return state
    }

    fun printChildren() {
        for (c in children) {
            // print the board, wins, visits, uct()
            val board = c.state.board
            val wins = c.wins
            val visits = c.visits
            val uctScore = c.uct()
            println("board: $board")
            println("wins: $wins, visits: $visits, ratio: ${(1.0)*wins/visits}")
            println("uct: $uctScore")
        }
    }

    companion object {
        val EXPLORATION_PARAM = 1.41
        val EXPLORATION_FACTOR =  1

        private fun newNodeFromState(state: BoardState): MCTSNode {
            val nextStates = state.generateNextStates().toMutableList()
            val newNode = MCTSNode(null, state, mutableListOf(), nextStates)
            return newNode
        }

        fun runFromState(root: BoardState, times: Int): MCTSNode {
            println("### ### ### MCTS ")
            val rootNode = newNodeFromState(root)

            var t = 0
            while (t < times) {
                // select
                val selected = rootNode.select()
                // expand
                val expanded = selected.expand()
                // keep track of rollout node
                val rolloutNode = expanded
                val result = rolloutNode.rollout(rootNode)
                rolloutNode.backPropagate(result)
                t++
            }

            println("MCTSRootNode and children...")
            println("state: ${rootNode.state.board} ${rootNode.state.attackingDirection}")
            println("rootNode: ${rootNode.wins}/${rootNode.visits}")

            val best = rootNode.getBestChild()
            println("best: ${best.state.board} ${best.wins}/${best.visits}")

            println("### children")
            rootNode.printChildren()
            println("### ### ### END")
            return rootNode
        }


    }

}