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

            // check whether current player is lost first
            if (state.isTerminalStateForPlayer(playerDir)) {
                return bestEvalOfPlayer(enemyDir)
            }
            // did our enemy lose?
            if (state.isTerminalStateForPlayer(enemyDir)) {
                return bestEvalOfPlayer(playerDir)
            }

            // TODO: for now, just return sum of piece scores
            val score: Double = state.board.findAllPiecesScore()

            return score

        }

        val MAXISMISER_BEST_EVAL = Double.POSITIVE_INFINITY
        val MINIMISER_BEST_EVAL = Double.NEGATIVE_INFINITY

        fun bestEvalOfPlayer(atkDir: Vector2D): Double {
            val isMaxPlayer = (atkDir == Vector2D.NORTH) && (atkDir != Vector2D.SOUTH)
            val bestEval = if (isMaxPlayer) MAXISMISER_BEST_EVAL else MINIMISER_BEST_EVAL
            return bestEval
        }





    }
}