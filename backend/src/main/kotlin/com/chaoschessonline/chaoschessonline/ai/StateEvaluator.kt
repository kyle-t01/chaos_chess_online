package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.ai.Minimax.Companion.playRandomlyTilTerminal
import com.chaoschessonline.chaoschessonline.model.BoardState
import com.chaoschessonline.chaoschessonline.util.Vector2D
import kotlin.math.abs

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
         * Find strategic score
         *
         * @param root
         * @return
         */
        fun findStrategicScore(root:BoardState): Double {
            // for this root play it 100 times to the end
            // assume that this is NOT a terminal state
            require(!root.isTerminalState()) {"ERROR: root must not be in a terminal state!!"}

            // TODO: consider for each child, play til terminal multiple times
            // But in this case, just play each root til terminal X times


            // keep track best child
            val startDepth = root.turnNumber;
            var t = 0
            val TIMES = 100
            var miniWinDepthSum = 0.0
            var maxiWinDepthSum = 0.0
            var miniWinsTotal = 0
            var maxiWinsTotal = 0
            while (t < TIMES) {
                // reach terminal for each child
                val terminal = playRandomlyTilTerminal(root)
                // collect stats such as average win depth, and total wins for each player
                val score  = StateEvaluator.findTacticalScore(terminal)
                if (!StateEvaluator.scoreIsTerminal(score)) {
                    // ignore cases where a draw or cannot be scored
                    t++;
                    continue;
                }
                // how many turns/depth has been played?
                val depthDiff = terminal.turnNumber - startDepth
                // track winDepths and # wins of maxi and mini player
                if (score == MAXISMISER_BEST_EVAL) {
                    // maxi player wins
                    maxiWinsTotal += 1
                    maxiWinDepthSum += depthDiff
                } else {
                    // mini player wins
                    miniWinsTotal += 1
                    miniWinDepthSum += depthDiff
                }
                t += 1
            }
            // finished evaluating this root TIME amount of times
            println("FINISHED PLAYING $TIMES games for this root")
            // generate statistics
            val maxiAverageWinDepth = maxiWinDepthSum / maxiWinsTotal
            val miniAverageWinDepth = miniWinDepthSum / miniWinsTotal
            var ourAverageWinDepth = 0.0
            var ourWinsTotal = 0
            // determine if we are maxi or mini player
            val isMaxiPlayer = root.attackingDirection == Vector2D.NORTH
            // set perspective
            if (isMaxiPlayer) {
                ourAverageWinDepth = maxiAverageWinDepth
                ourWinsTotal = maxiWinsTotal
            }  else {
                ourAverageWinDepth = miniAverageWinDepth
                ourWinsTotal = miniWinsTotal
            }
            // interested in finding "proportion of potential wins", "how fast can it win", "how much ahead of enemy it is"
            // "prop. of wins" = our wins / ( miniWinsTotal +  maxiWinsTotal)
            val propOfWins = (1.0) * ourWinsTotal / (miniWinsTotal + maxiWinsTotal)

            // "how fast can we win?" =  1 / average OUR winning depth
            val fastWin = 1 / (ourAverageWinDepth)

            // "how far ahead are we?" = 1 / abs(maxiAverageWinDepth - miniAverageWinDepth); take complement if diff is negative
            val isAhead = ourAverageWinDepth < (maxiAverageWinDepth + miniAverageWinDepth - ourAverageWinDepth)
            val catchUpEase= 1 / abs(maxiAverageWinDepth - miniAverageWinDepth)
            val farAheadValue = if (isAhead) (1-catchUpEase) else (catchUpEase);

            println("# stats")
            println("we are maximising player (attackNORTH) $isMaxiPlayer")
            println("maxWins, depth = $maxiWinsTotal, $maxiAverageWinDepth | minWins, depth = $miniWinsTotal, $miniAverageWinDepth")
            println("propwins: $propOfWins, fastWin: $fastWin, farAheadValue: $farAheadValue")
            return (propOfWins*fastWin*farAheadValue)
        }
    }
}