package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.ai.NextStateMaker.Companion.playRandomlyTilTerminal
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
            if (state.isTerminalStateForCurrentPlayer()) {
                return bestEvalOfPlayer(enemyDir)
            }
            // did our enemy lose?
            if (state.flipPlayer().isTerminalStateForCurrentPlayer()) {
                return bestEvalOfPlayer(playerDir)
            }

            // TODO: for now, just return sum of piece scores
            val score: Double = state.board.findAllPiecesScore()

            // Are we under check from this action? then bad

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

            // keep track best child
            val startDepth = root.turnNumber;
            var t = 0
            val TIMES = 100
            val maxiDepths: MutableList<Int> = mutableListOf()
            val miniDepths: MutableList<Int> = mutableListOf()
            val isMaxiPlayer = root.attackingDirection == Vector2D.NORTH
            while (t < TIMES) {
                // reach terminal for each child
                val terminal = playRandomlyTilTerminal(root, 100)
                val depthDiff = terminal.turnNumber - startDepth
                // collect stats such as average win depth, and total wins for each player
                val score  = findTacticalScore(terminal)
                if (!scoreIsTerminal(score)) {
                    // in cases where board is not a terminal board, count as LOSS
                    if (findTacticalScore(terminal) >= 0.0) {
                        maxiDepths.add(depthDiff)
                    } else {
                        miniDepths.add(depthDiff)
                    }
                    //require(false) {"ERROR: somehow ended up here, should be a terminal state! ${terminal.board}"}
                    t++;
                    continue;
                }
                // how many turns/depth has been played?

                // track winDepths and # wins of maxi and mini player
                if (score == MAXISMISER_BEST_EVAL) {
                    // maxi player wins
                    maxiDepths.add(depthDiff)
                } else {
                    // mini player wins
                    miniDepths.add(depthDiff)
                }
                t += 1
            }
            // finished evaluating this root TIME amount of times
            //println("FINISHED PLAYING $TIMES games for this root")
            // generate statistics
            // find out which Depths is ours and which Depths is the enemy
            var ourDepths:MutableList<Int> = mutableListOf()
            var enemyDepths:MutableList<Int> = mutableListOf()

            // set perspective (not very elegant, but works for now)
            if (isMaxiPlayer) {
                ourDepths= maxiDepths
                enemyDepths = miniDepths
            }  else {
                ourDepths = miniDepths
                enemyDepths = maxiDepths
            }

            val totalWins = maxiDepths.size + miniDepths.size

            println("maxiDepths: ${maxiDepths.average()} [${maxiDepths.size}] win in ${maxiDepths.min()}, miniDepths: ${miniDepths.average()} [${miniDepths.size}] win in ${miniDepths.min()}")
            // interested in finding "proportion of potential wins", "how fast can it win", "how much ahead of enemy it is"
            // "prop. of wins" = our wins / ( miniWinsTotal +  maxiWinsTotal)
            val propOfWins = (1.0) * ourDepths.size / totalWins

            // "how fast can we win?" =  1 / average OUR winning depth (assumes enemy is reallly stupid)
            val fastWin = 1 / (ourDepths.min() + 1.0)

            // "how far ahead are we?"
            val depthDiff = enemyDepths.average() - ourDepths.average() //large is good
            val sigmoidGap = 1.0 / (1.0 + exp(0.2*-depthDiff))
            println("where depthDiff is $depthDiff, gives this score: $sigmoidGap")
            val farAheadValue = sigmoidGap

            // "naive play": "from playing randomly, how fast could enemy? (we did in fact play randomly)"
            val enemyFastestWinDepth = enemyDepths.min()
            val ourFastestWinDepth = ourDepths.min()

            // if by random play, enemy can beat us in 1 turn(s), not safe at all
            // if we can beat enemy in 1 turns, 100% a safe position to take
            var safety = 1.0
            val fastWinDiff = enemyFastestWinDepth - ourFastestWinDepth
            val sigmoidFast = 1.0 / (1.0 + exp(0.2*-fastWinDiff)) + 0.2
            println("### ### ###")
            println("fastWinDiff = $fastWinDiff")
            println("sigmoidFast (new safety)= $sigmoidFast")
            safety = Math.min(sigmoidFast, 1.0)
            if (enemyFastestWinDepth == 1) {
                // we will lose when enemy moves
                safety = Double.NEGATIVE_INFINITY
            }
            if (ourFastestWinDepth == 1) {
                // we will win next move
                safety = Double.POSITIVE_INFINITY
            }

            // play with enemy fastest win depth later




            println()
            val a = 0.05
            val b = 0.00
            val c = 10
            val d = 1.0
            println("# stats fo this child ${root.board} ${root.attackingDirection}")
            println("propwins: $propOfWins, fastWin: $fastWin, farAheadValue: $farAheadValue")
            println("ourFastWin = $ourFastestWinDepth, enemyFastWin = $enemyFastestWinDepth")
            println("safety (relative to us): $safety")
            //println("qwf: $quickWinFactor, safety: $safety")
            return safety*(a*propOfWins+ b*fastWin+ c*farAheadValue) * root.attackingDirection.row
        }

        /**
         * Evaluate state based on combined tactical and strategic score
         *
         * @param root
         * @return
         */
        fun evaluateState(root: BoardState): Double {
            val tacticalScore = findTacticalScore(root)
            // if tacticalScore is already terminal, return
            if (scoreIsTerminal(tacticalScore)) {
                println("This is a terminal: $tacticalScore")
                println("results in this board: ${root.board}")
                return tacticalScore
            }
            // otherwise, factor in the strategic score
            val strategicScore = findStrategicScore(root)
            // combine the two scores
            val tacticalWeight = 0.00 //tactical weight is "very greedy", punish greed
            val strategicWeight = 1.0
            val finalScore =(tacticalScore*tacticalWeight) + (strategicScore*strategicWeight)
            println("finalscore: $finalScore (weighted), tacticalScore: $tacticalScore, strat: $strategicScore (all unweighted)")
            println("results in this board: ${root.board}")
            println("### ### ###")
            return finalScore
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