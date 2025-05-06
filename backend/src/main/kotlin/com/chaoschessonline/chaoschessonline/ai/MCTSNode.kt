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
    var wins = 0
    var visits = 0




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
        val score = StateEvaluator.findTacticalScore(terminalState)
        // get perspective of root
        if (!StateEvaluator.scoreIsTerminal(score)) {
            // if not terminal score, then likely a draw
            println("got this score instead $score")
            println("the board was ${terminalState.board}")
            return 0.5
        }

        // if this score is best score for the ROLLOUT player, count as ROOT win
        if (score == StateEvaluator.bestEvalOfPlayer(root.state.attackingDirection)) {
            return 1.0
        }
        return 0.0
    }

    fun backPropagate(result: Double) {
        var curr: MCTSNode? = this
        while (curr != null) {
            curr.visits += 1
            curr.wins += result.toInt()
            curr = curr.parent
        }
    }

    fun uct(): Double {
        // uct score is always +ve regardless the player at the state
        if (visits == 0) return Double.POSITIVE_INFINITY
        val N = parent?.visits ?: 1
        val n = visits
        val w = wins
        val logN = Math.log(N.toDouble())
        val exploit = (1.0)*w/n
        val explore = EXPLORATION_PARAM * sqrt(logN/n)
        return exploit + explore
    }

    fun getBestChild(): MCTSNode {
        return children.maxBy{ it.visits}
    }

    fun toState(): BoardState {
        return state
    }

    

    companion object {
        val EXPLORATION_PARAM = 1.4
        val EXPLORATION_FACTOR =  1

        private fun newNodeFromState(state: BoardState): MCTSNode {
            val nextStates = state.generateNextStates().toMutableList()
            val newNode = MCTSNode(null, state, mutableListOf(), nextStates)
            return newNode
        }

        fun runFromState(root: BoardState, times: Int): MCTSNode {
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
            println("nodeSTatesL: ${rootNode.wins}/${rootNode.visits}")

            val best = rootNode.getBestChild()
            println("best: ${best.state.board} ${best.wins}/${best.visits}")
            return rootNode
        }


    }

}