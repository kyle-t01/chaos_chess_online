package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.ai.NextStateMaker
import com.chaoschessonline.chaoschessonline.model.BoardState
import com.chaoschessonline.chaoschessonline.util.Vector2D
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sign

/**
 * State evaluator
 *
 * evaluation logic abstracted from BoardState class
 *
 * @constructor Create empty State evaluator
 */
class StateEvaluator {
    companion object {

        /**
         * Find tactical score
         *
         * @param state
         * @return
         */
        fun findTacticalScore(state: BoardState): Double {
            // findTacticalScore is usually called on child of a root node

            // always evaluate from player's perspective
            val playerDir = state.attackingDirection
            val enemyDir = playerDir.reflectRow()

            // check whether current player is lost first
            if (state.hasPlayerLost()) {
                return bestEvalOfPlayer(enemyDir)
            }
            // did our enemy lose?
            if (state.hasEnemyLost()) {
                return bestEvalOfPlayer(playerDir)
            }
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

        /**
         * Score is terminal (whether +inf or -inf)
         *
         * @param score
         * @return
         */
        fun scoreIsTerminal(score: Double): Boolean {
            return score == Double.NEGATIVE_INFINITY || score == Double.POSITIVE_INFINITY
        }

        /**
         * Sigmoid
         *
         * @param score
         * @param factor
         * @param constant
         * @return
         */
        fun sigmoid(score: Double, factor: Double):Double {
            return 1.0 / (1.0 + exp(factor*-score))
        }
    }
}