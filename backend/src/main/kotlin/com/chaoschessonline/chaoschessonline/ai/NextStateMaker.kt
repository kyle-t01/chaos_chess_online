package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.util.Vector2D
import com.chaoschessonline.chaoschessonline.model.BoardState
import com.chaoschessonline.chaoschessonline.model.ValidActionGenerator
import kotlin.collections.ArrayDeque

import kotlin.random.Random

/**
 * NextStateMaker
 *
 * @constructor Create empty NextStateMaker
 */

class NextStateMaker {

    companion object {
        /**
         * Is maximising player the current player?
         *
         * @param root
         * @return
         */
        fun isMaximisingPlayer(root: BoardState): Boolean {
            return root.attackingDirection == Vector2D.NORTH
        }


        fun makeNextState(root: BoardState): BoardState {
            return makeNextStateMCTS(root)
        }

        fun makeNextStateMCTS(root: BoardState): BoardState {
            val rootNode = MCTSNode.fromBoardState(root)
            rootNode.run(10000)
            return rootNode.getBestChild().toState()
        }


        /**
         * Traverse level order (deprecated - do not use)
         *
         * @param root
         */
        fun traverseLevelOrder(root: BoardState) {
            // (0) if a terminal state, exit
            if (root.isTerminalState()) return

            // queue
            val queue: ArrayDeque<BoardState> = ArrayDeque<BoardState>()
            queue.addLast(root)

            // visitation array (actually a set of states that we generated children for)
            val visited: MutableSet<String> = mutableSetOf()

            // level sizes of each depth
            val levelSizes: MutableList<Int> = mutableListOf()

            // time it took for one level
            val levelTimes: MutableList<Long> = mutableListOf()

            // branching factor on average
            val branchingFactors: MutableList<Double> = mutableListOf()


            // traverse this level and find all possible next states for this level
            var depth = 0;
            while (!queue.isEmpty() && depth <= 4) {
                // level-order traversal: find number of branches in this level
                val levelSize = queue.size
                levelSizes.add(levelSize)
                val startTime = System.currentTimeMillis()
                println("### LEVEL $depth ###")
                println(">> levelSize: $levelSize")

                // for all children in this level
                for (x in 0..levelSize - 1) {
                    val curr = queue.removeFirst()

                    // get the hash
                    val hash = curr.board.board.joinToString("") + curr.attackingDirection

                    // if visited continue
                    if (hash in visited) continue

                    // mark this as visited
                    visited.add(hash)

                    // if this state is a terminal state, don't bother generating children

                    if (curr.isTerminalState()) continue

                    // (1) find all possible actions of one child
                    val pieces = curr.findCurrentAttackingPieces()
                    val actions = ValidActionGenerator.findAllValidActions(root)
                    for (a in actions) {
                        // apply each action to get a board state
                        val next = curr.applyAction(a)

                        val childHash = next.board.board.joinToString("") + next.attackingDirection
                        // should be 0(1) search
                        if (childHash in visited) continue

                        // does not account for if already in queue, but not visited yet
                        // but OK since when pop(), will check against whether visited anyways
                        //NOT WORTH the extra 0(n) time in linear search using queue.contains(next)

                        // if (queue.contains(next)) continue

                        queue.addLast(next)
                    }
                }
                // track time taken for this level
                val timeTaken = System.currentTimeMillis() - startTime
                levelTimes.add(timeTaken)
                println(">> levelTime: $timeTaken")
                println(">> unique visited states: ${visited.size}")

                depth += 1
            }
            println("--STATS--")
            println("levelSizes: $levelSizes")
            println("final depth reached is $depth")
            println("unique board states is ${visited.size}")
        }


        fun playRandomlyTilTerminalSmartReply(root: BoardState, maxDepth: Int): BoardState {
            // "Smart Reply", enemy will never try to play a move that causes immediate loss
            var curr = root
            val visited: MutableSet<String> = mutableSetOf()
            var depth = 0

            // explore children of root with sampling
            while ((depth < maxDepth)) {
                // if terminal return
                if (curr.isTerminalState()) {
                    break
                }
                // mark this as visited
                visited.add(curr.toHashStr())
                // generate unvisited next states (not threat aware)
                val unvisited = curr.generateThreatAwareNextStates().filter{(it.toHashStr() !in visited)}
                // if visited everything, we are done

                if (unvisited.isNotEmpty()) {
                    curr = unvisited.shuffled()[0]
                }
                depth += 1
            }
            return curr
        }

    }
}
