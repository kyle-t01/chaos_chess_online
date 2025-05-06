package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.model.Action
import com.chaoschessonline.chaoschessonline.model.BoardState
import com.chaoschessonline.chaoschessonline.model.ValidActionGenerator
import com.chaoschessonline.chaoschessonline.util.Vector2D
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
        var curr = this
        // find leaf node
        // while tried every state of this node, and have a next child
        while(untriedStates.isEmpty() && children.isNotEmpty()) {
            // select in a minimax fashion, attack NORTH = maximiser
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
            // if not terminal score, then likely a draw, count as loss
            return 0.0
        }

        // if this score is best score for the ROOT player, count as root win
        if (score == StateEvaluator.bestEvalOfPlayer(root.state.attackingDirection)) {
            return 1.0
        }
        return 0.0
    }

    fun backPropagate(result: Double) {
        // backPropagate must be called on the rollout node NOT terminal
        var curr: MCTSNode? = this
        // perspective
        val rolloutTeam = this.state.attackingDirection
        while (curr != null) {
            curr.visits += 1
            val currentTeam = curr.state.attackingDirection
            curr.wins += if (currentTeam == rolloutTeam) result.toInt() else (1-result).toInt()
            curr = curr.parent
        }
    }

    fun uct(): Double {
        // uct score is always +ve regardless the player at the state
        if (visits == 0) return Double.POSITIVE_INFINITY
        val N = parent?.visits ?: 1
        val logN = Math.log(N.toDouble())
        val exploit = wins.toDouble()/visits
        val explore = EXPLORATION_PARAM * sqrt(logN/N)
        return exploit + explore
    }



    companion object {
        val EXPLORATION_PARAM = 1.4

        private fun newNodefromState(state: BoardState): MCTSNode {
            val nextStates = state.generateNextStates().toMutableList()
            val newNode = MCTSNode(null, state, mutableListOf(), nextStates)
            return newNode
        }

        fun runFromState(root: BoardState): MCTSNode {
            val rootNode = newNodefromState(root)
            //


            return rootNode
        }
    }

}