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
            // hardcode to run 10,000 times for noe
            val rootNode = MCTSNode.runFromState(root, 10000)
            return rootNode.getBestChild().toState()
        }

        /**
         * Make next state based on custom agent and evaluation implementation
         *
         * @param root
         * @return
         */
        fun makeNextStateCustom(root: BoardState): BoardState {
            // (1) get all possible child states
            val nextStates = root.generateNextStates()
            require(nextStates.size >0) {"makeNextState(): no possible moves, this should not have happened!!"}
            val nextEvals:MutableList<Double> = mutableListOf()
            // (2) evaluate each child
            for (next in nextStates) {
                val eval = StateEvaluator.evaluateState(next)
                nextEvals.add(eval)
            }
            // (3) get bestScore of the current player
            val bestScore = if (isMaximisingPlayer(root)) nextEvals.max() else nextEvals.min()
            // (4) find the nextState that has this bestScore
            val idx = nextEvals.indexOf(bestScore)
            val desiredNextState = nextStates[idx]
            println("nextEvals = $nextEvals, pickedEval = $bestScore, boardState = ${desiredNextState.board}")
            return desiredNextState
        }


        fun makeNextStateRandom(state: BoardState): BoardState {
            // make a random action, depending on current boardstate
            val ownPieces = state.findCurrentAttackingPieces()
            if (ownPieces.size == 0) return state;

            println("###makeRandomAction()###")
            println("##Attacking Pieces:")
            for (p in ownPieces) {
                println("$p: ${state.board.getPieceChar(p)}")
            }
            println("####")

            // for all pieces, find the first piece that has a valid destination
            var src: Int = -1
            val validDest: MutableList<Int> = mutableListOf()
            for (p in ownPieces) {
                val destList = ValidActionGenerator.findPossibleActionsForIndex(p, state)
                if (destList.size > 0) {
                    validDest.addAll(destList)
                    src = p
                    break
                }
            }
            if (validDest.size == 0) return state;
            println("# picked SRC piece $src: ${state.board.getPieceChar(src)}")


            // select a destination found
            println("validDests = $validDest")
            val dest: Int = validDest[Random.nextInt(0, validDest.size)]
            println("# picked DEST piece $dest: ${state.board.getPieceChar(dest)}")

            val newState = state.applyAction(src, dest)

            return newState
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
        fun traverseRandomlyFromBookMoves(root: BoardState) {
            // (0) if a terminal state, exit
            if (root.isTerminalState()) return

            // queue
            val queue:ArrayDeque<BoardState> = ArrayDeque<BoardState>()
            queue.addLast(root)

            // visitation array (actually a set of states that we generated children for)
            val visited: MutableSet<String> = mutableSetOf()

            // level sizes of each depth
            val levelSizes:MutableList<Int> = mutableListOf()

            // time it took for one level
            val levelTimes:MutableList<Long> = mutableListOf()

            // branching factor on average
            val branchingFactors:MutableList<Double> =  mutableListOf()


            // traverse this level and find all possible next states for this level
            var depth = 0;
            while(!queue.isEmpty() && depth < 2) {
                // level-order traversal: find number of branches in this level
                val levelSize = queue.size
                levelSizes.add(levelSize)
                val startTime = System.currentTimeMillis()
                println("### LEVEL $depth ###")
                println(">> levelSize: $levelSize")

                // for all children in this level
                for (x in 0..levelSize-1){
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

                        val childHash = next.toHashStr()
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

            // here, traverse it in DFS fashion, rename it to stack
            val stack = queue
            var results:Vector2D = Vector2D(0,0)
            var index = 0;
            val size = stack.size
            while(!stack.isEmpty()) {
                val bookState = stack.removeLast()
                var maxWins = 0
                var minWins = 0
                var depth = 0
                for (i in 1..100) {

                    val terminal = playRandomlyTilTerminal(bookState)
                    //println("${terminal.board} ${terminal.turnNumber} ${StateEvaluator.evaluate(terminal)}")
                    val score = StateEvaluator.findTacticalScore(terminal)
                    if (!(score == Double.NEGATIVE_INFINITY || score == Double.POSITIVE_INFINITY)) {
                        continue
                    }
                    if (score == Double.POSITIVE_INFINITY) {
                        maxWins += 1

                    } else {
                        minWins +=1
                    }

                }

                results += Vector2D(maxWins, minWins)

                println("### BOOK MOVE ${index+1} / $size ###")
                println("explored one book state of ${bookState.board}")
                println("maxWins = $maxWins, minWins = $minWins")

                // looks like chess player will win 42% of time
                // xiang qi will win 58% of the time
                println("### END BOOK MOVE")
                index +=1
            }


            println("--STATS--")
            println("levelSizes: $levelSizes")
            println("results = $results")
            println("final depth reached is $depth")
            println("unique board states is ${visited.size}")
        }

        /**
         * Play randomly til terminal
         *
         * @param root
         * @return
         */
        fun playRandomlyTilTerminal(root: BoardState): BoardState {
            val stack: ArrayDeque<BoardState> = ArrayDeque()
            stack.addLast(root)
            val visited: MutableSet<String> = mutableSetOf()

            // explore children of root with sampling
            while (!stack.isEmpty()) {
                val curr = stack.removeLast()
                // if visited continue
                if (curr.toHashStr() in visited) continue

                // mark this as visited
                visited.add(curr.toHashStr())
                if (curr.isTerminalState()) {
                    return curr
                }
                val nextStates = curr.generateNextStates()
                if (nextStates.isEmpty()) return curr
                val freshStates = nextStates.filter { (it.toHashStr() !in visited) }
                if (freshStates.isEmpty()) return curr
                val sampleNext = freshStates.shuffled()[0]
                stack.addLast(sampleNext)
            }
            return root
        }

        /**
         * Play random simulation (deprecated, for stats purposes only)
         *
         * @param root
         * @return
         */
        fun playRandomSimulation(root: BoardState): BoardState {
            if (root.isTerminalState()) {
                return root
            }
            val startTime = System.currentTimeMillis()

            // immediately generate add all children
            val nextStates = root.generateNextStates()
            // for each state, play til terminal state
            for (next in nextStates) {
                val initialDepth = next.turnNumber
                val maxiDepths: MutableList<Int> = mutableListOf()
                val miniDepths: MutableList<Int> = mutableListOf()
                for (plays in 1..100) {

                    val terminal = playRandomlyTilTerminal(next)
                    val score = StateEvaluator.findTacticalScore(terminal)


                    if (!(score == Double.NEGATIVE_INFINITY || score == Double.POSITIVE_INFINITY)) {
                        continue
                    }
                    // bug should be from current player's perspective?
                    val depthDiff = terminal.turnNumber - initialDepth
                    if (score == Double.POSITIVE_INFINITY) {
                        maxiDepths.add(depthDiff)
                    } else {
                        miniDepths.add(depthDiff)
                    }
                    println("terminal ${terminal.board} score: $score depth: ${terminal.turnNumber}")

                    /*
                    var par = terminal.parent
                    while(par != null) {
                        // println("parent ${par.board} at depth: ${par.turnNumber}")
                        par = par.parent

                    }

                     */

                }
                println("maxWins = ${maxiDepths.size}, minWins = ${miniDepths.size}")
                val aveMaxWinDepth:Double = maxiDepths.average()
                val aveMinWinDepth:Double = miniDepths.average()

                println(">> maxPlayer ave win depth = $aveMaxWinDepth, minPlayer ave win depth = $aveMinWinDepth")
                maxiDepths.sort()
                miniDepths.sort()
                println(">> maxiList = $maxiDepths, miniList = $miniDepths")
                println(">> totalWins = ${maxiDepths.size + miniDepths.size}")
                val sum = maxiDepths.sum() + miniDepths.sum()
                println(">> average depth to win = ${sum/(maxiDepths.size + miniDepths.size)}")
                println(">> ")
                println("##### END SIMULATION FOR ONE CHILD #####")
            }
            val endTime = System.currentTimeMillis()
            println("### simulating each child X times takes ${endTime-startTime}")

            return root
        }





    }
}
