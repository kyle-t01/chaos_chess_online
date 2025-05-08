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
        val newUntriedStates = nextState.generateThreatAwareNextStates().toMutableList()

        // make a new MCTS node
        val child = MCTSNode(this, nextState, mutableListOf(), newUntriedStates)
        children.add(child)
        return child
    }

    fun rollout(root: MCTSNode): Double {
        val terminalState = NextStateMaker.playRandomlyTilTerminalSmartReply(state, MAX_DEPTH)
        val terminalEval = StateEvaluator.findTacticalScore(terminalState)
        val rootPlayer = root.state.attackingDirection
        // is terminal state caused by rootPlayer?
        //println("terminalState caused by root player: ${terminalPlayer != rootPlayer}, $score, $terminalScore")
        // get perspective of root

        if (!StateEvaluator.scoreIsTerminal(terminalEval)) {
            // if not terminal score, then likely a draw so not a win
            //println(">>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<")
            val score = StateEvaluator.sigmoid(terminalEval * rootPlayer.row, 0.2)
            //println("$terminalEval (objective eval), with $score")
            //println("the board was ${terminalState.board}")
            //println(">>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<<<<<<")
            return 0.0 //return score later
        }

        val bestEvalPlayer = StateEvaluator.bestEvalOfPlayer(rootPlayer)
        if (bestEvalPlayer == terminalEval) {
            // for now, return whether we have won
            return 1.0
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


    fun run(times: Int): MCTSNode {
        val rootNode = this
        // before running MCTS, if there is an immediate win, take it
        // look at untried States
        val startTime = System.currentTimeMillis()
        var immediateWin = false
        println("<<>>>")
        println("root player: ${state.attackingDirection}")
        for (s in untriedStates) {
            // if on nextState, enemy (player of next state) LOST, then skip MCTS
            if (s.isTerminalForCurrentPlayer()) {
                println("next player: ${s.attackingDirection}")
                println("this board: ${s.board}")
                // found an immediate win for us, add that as child
                val winner = MCTSNode(this, s, mutableListOf(), mutableListOf())
                children.add(winner)
                immediateWin = true
                break;

            }
        }
        println("<<>>>")
        // no immediate win, run MCTS
        if (!immediateWin) {
            println("run() did not find an immediate win! running MCTS...")
            rootNode.runMCTS(times)
        }

        // optional printing of stats
        println("## MCTSRootNode and children... ##")
        println("state: ${rootNode.state.board} ${rootNode.state.attackingDirection}")
        println("rootNode: ${rootNode.wins}/${rootNode.visits}")

        val best = rootNode.getBestChild()
        println("best: ${best.state.board} ${best.wins}/${best.visits}")
        println(">> besthash: ${best.state.toHashStr()}")
        println("children")
        rootNode.printChildren()
        println("### ### ### END")
        val endTime = System.currentTimeMillis()
        val totalTime = endTime-startTime
        println("it took this much time: $totalTime")
        // return the root reference regardless
        return this
    }

    private fun runMCTS(times: Int): MCTSNode {
        val rootNode = this
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
        // return rootNode regardless
        return rootNode
    }

    private fun pruneUntriedStates() {

    }

    companion object {
        val EXPLORATION_PARAM = 2.0
        val EXPLORATION_FACTOR =  1
        val MAX_DEPTH = 7

        fun fromBoardState(state: BoardState): MCTSNode {
            val nextStates = state.generateNextStates().toMutableList()
            val newNode = MCTSNode(null, state, mutableListOf(), nextStates)
            return newNode
        }

    }

}