package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.model.BoardState
import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * State evaluator
 *
 * evaluation logic abstracted from BoardState class
 *
 * @constructor Create empty State evaluator
 */
class StateEvaluator {
    companion object {
        fun evaluate(state: BoardState): Double {

            // TODO: refactor isTerminalStateForPlayer() to two seperate functions (don't expose state.attackingDir..)

            // always evaluate from player's perspective
            val playerDir = state.attackingDirection
            val enemyDir = playerDir.reflectRow()
            val isMaxPlayer = (playerDir == Vector2D.NORTH)
            // there is probs some more efficient way of doing this...
            val worstEval = if (isMaxPlayer) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
            val bestEval = if (isMaxPlayer) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
            // check whether current player is lost first
            if (state.isTerminalStateForPlayer(playerDir)) {
                return worstEval
            }
            // did our enemy lose?
            if (state.isTerminalStateForPlayer(enemyDir)) {
                return bestEval
            }

            // now just simply count difference in the number of pieces (assign scores later)
            val minPieces = state.findAttackingPieces(Vector2D.SOUTH)
            val maxPieces = state.findAttackingPieces(Vector2D.NORTH)

            val score: Int = maxPieces.size - minPieces.size

            return score.toDouble()

        }



    }
}