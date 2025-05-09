package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.model.*
import com.chaoschessonline.chaoschessonline.util.Vector2D

class ValidActionGenerator {

    companion object {
        /**
         * Find all valid actions
         *
         * @param state
         * @return
         */
        fun findAllValidActions(state: BoardState): List<Action> {
            // find attacking pieces
            val pieces = state.findCurrentAttackingPieces()
            // if no leader return empty list
            val ourLeaderPieces = state.findLeaderPositions()

            if (!state.board.isLeaderInPositions(pieces)) return listOf()
            // if only leader piece left
            if (pieces.size == 1) return listOf()


            val enemyPieces = state.findCurrentEnemyPieces()
            val enemyLeaderPieces = state.findEnemyLeaderPositions()
            require(enemyPieces.isNotEmpty()) {"somehow finding valid actions when enemy leader dead"}
            // assume that there is only one enemy leader
            val enemyLeaderPiece = enemyLeaderPieces[0]

            // find all possible actions
            // temporary workaround
            // TODO: right now converting Ints to Vector2Ds (pieces is List<Int>), Board class should have pre-converted
            val ourPositions = pieces.map{Vector2D.fromIndex(it, Board.DEFAULT_DIMENSION)}
            val actions:List<Action> = findActionsFromPosList(ourPositions, state)

            // can we kill enemy leader immediately?
            val actionsThatKillEnemyLeader = actions.filter{it.to == enemyLeaderPiece}
            // yes, can win immediately
            if (actionsThatKillEnemyLeader.isNotEmpty()) return actionsThatKillEnemyLeader
            // no, can't win immediately


            // TODO: actions that result us in threat are disallowed (should be handled by class that deals with state)
            return actions
        }

        /**
         * Find actions from pos list
         *
         * @param positions
         * @param state
         * @return
         */
        private fun findActionsFromPosList(positions: List<Vector2D>, state: BoardState): List<Action> {
            val actions:MutableList<Action> = mutableListOf()
            for (srcPos in positions) {
                val validDests = findPossibleActionsForIndex(srcPos.getIndex(Board.DEFAULT_DIMENSION), state)
                for (dest in validDests) {
                    val destPos = Vector2D.fromIndex(dest, Board.DEFAULT_DIMENSION)
                    val action = Action(srcPos, destPos)
                    actions.add(action)
                }
            }
            return actions
        }

        /**
         * Find all enemy threats
         *
         * @param state
         * @return
         */
        fun findAllEnemyThreats(state: BoardState): List<Action> {
            // flip the board
            val enemyPerspective = state.flipPlayer()
            return findAllValidActions(enemyPerspective)
        }

        /**
         * Find possible actions for index (actions = moves + attacks + any other special actions...)
         *
         * @param index
         * @param state
         * @return List<Int> of possible actions
         */
        fun findPossibleActionsForIndex(index: Int, state: BoardState): List<Int> {
            // assume that index always within bounds, and called by moving player
            val c = state.board.board[index]
            val pieceChar = c.uppercaseChar()
            var possibleEndIndices: List<Int> = listOf();
            when (pieceChar) {
                'P' -> {
                    possibleEndIndices = findPawnActions(index, state)
                }

                'R' -> {
                    possibleEndIndices = findRookActions(index, state)
                }

                'B' -> {
                    possibleEndIndices = findBishopActions(index, state)
                }

                'Q' -> {
                    possibleEndIndices = findQueenActions(index, state)
                }

                'K' -> {
                    possibleEndIndices = findKingActions(index, state)
                }
                // xiangqi pieces
                'Z' -> {
                    possibleEndIndices = findFootSoldierActions(index, state)
                }

                'M' -> {
                    possibleEndIndices = findHorseActions(index, state)
                }

                'S' -> {
                    possibleEndIndices = findScholarActions(index, state)
                }

                'G' -> {
                    possibleEndIndices = findGeneralActions(index, state)
                }

                'C' -> {
                    possibleEndIndices = findCannonActions(index, state)
                }
                ' ' -> return listOf()
                else -> {
                    println("ERROR: Unimplemented or Unknown pieceChar!!!")
                }
                //'B' -> {possibleEndIndices = findPawnActions(index, state)}
                //'N' -> findKnightActions()
                /*

                'Q' ->
                'K' ->
                'Z' ->
                'S' ->
                'X' ->
                'M' ->
                'J' ->
                'C' ->
                'G' ->
                */

            }
            return possibleEndIndices
        }



        /**
         * Find pawn actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findPawnActions(index: Int, state: BoardState): List<Int> {
            // unit vector for attack direction

            val initialPos = Board.getPositionFromIndex(index)
            val thisChar: Char = state.board.board[index]
            val attackDirection = PieceType.findAttackDirection(thisChar)
            val possibleEndIndices: MutableList<Int> = mutableListOf()

            // look at possible movement
            var dist = 1
            while (dist <= 2) {
                // check some end positions
                val endPos = initialPos + (attackDirection * dist)
                if (!Board.positionInsideBounds(endPos)) break;
                val endIndex = Board.getIndexFromPosition(endPos)

                // if end position is empty, then can move there
                if (state.board.board[endIndex] == ' ') {
                    possibleEndIndices.add(endIndex)
                } else {
                    break;
                }
                dist += 1
            }

            // look at possible attacks
            val endPosNW = initialPos + (Vector2D.NW * attackDirection.row)
            val endPosNE = initialPos + (Vector2D.NE * attackDirection.row)
            val attackPositions: List<Vector2D> = listOf(endPosNE, endPosNW)

            for (pos in attackPositions) {
                if (!Board.positionInsideBounds(pos)) continue
                // within bounds
                val thatChar = state.board.getPieceChar(pos)
                if (!PieceType.isEnemy(thisChar, thatChar)) continue
                // is an enemy, so valid attack action
                val endIndex = Board.getIndexFromPosition(pos)
                possibleEndIndices.add(endIndex)

            }

            return possibleEndIndices

        }

        /**
         * Find foot soldier actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findFootSoldierActions(index: Int, state: BoardState): List<Int> {
            // unit vector for attack direction

            val initialPos = Board.getPositionFromIndex(index)
            val thisChar: Char = state.board.board[index]
            val attackDirection = PieceType.findAttackDirection(thisChar)
            val possibleEndIndices: MutableList<Int> = mutableListOf()

            // before no mans land: move forward, attack forward
            // after no mans land: move and attack UP, LEFT and RIGHT

            val possibleActionVectors: List<Vector2D> = listOf(attackDirection, Vector2D.EAST, Vector2D.WEST)

            for (dir in possibleActionVectors) {
                val endPos: Vector2D = initialPos + dir
                if (!Board.positionInsideBounds(endPos)) continue
                // if endPos has an ally, not allowed to go there
                val thatChar: Char = state.board.getPieceChar(endPos)
                if (PieceType.isAlly(thisChar, thatChar)) continue
                possibleEndIndices.add(Board.getIndexFromPosition(endPos))

                // if we have NOT crossed half of the board. then ignore other moves
                if (!Board.isPositionInNorth(initialPos)) break
            }
            return possibleEndIndices
        }

        /**
         * Find slider actions (such as Rook, Chariot, Queen, Bishop)
         *
         * TODO: have maxDistance as a parameter
         *
         * @param index
         * @param state
         * @param directions
         * @return
         */
        fun findSliderActions(index: Int, state: BoardState, directions: List<Vector2D>): List<Int> {
            // TODO: index, state, unitMovement, unitAttack, moveDist, attackDist, moveDirs, attackDirs, attackReqs

            val initialPos = Board.getPositionFromIndex(index)
            val thisChar: Char = state.board.board[index]
            // val attackDirection = PieceType.findAttackDirection(thisChar)
            val possibleEndIndices: MutableList<Int> = mutableListOf()


            // extend in each unit direction, can move there as long as not an ally
            for (dir in directions) {
                // extend in each direction until blocked or out-of-bounds
                var endPos = initialPos + dir
                while (Board.positionInsideBounds(endPos)) {

                    // if an ally, it is blocked
                    val thatChar: Char = state.board.getPieceChar(endPos)
                    if (PieceType.isAlly(thisChar, thatChar)) {
                        break
                    };
                    possibleEndIndices.add(Board.getIndexFromPosition(endPos))
                    // if hit an enemy, do not look further
                    if (PieceType.isEnemy(thisChar, thatChar)) {
                        break
                    }
                    endPos += dir
                }
            }

            return possibleEndIndices
        }

        /**
         * Find rook actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findRookActions(index: Int, state: BoardState): List<Int> {
            // Rook moves
            val unitDirections: List<Vector2D> = Vector2D.STRAIGHTS
            return findSliderActions(index, state, unitDirections)
        }

        /**
         * Find bishop actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findBishopActions(index: Int, state: BoardState): List<Int> {

            // Bishop moves
            val unitDirections: List<Vector2D> = Vector2D.DIAGONALS
            return findSliderActions(index, state, unitDirections)
        }

        /**
         * Find queen actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findQueenActions(index: Int, state: BoardState): List<Int> {
            val unitDirections: List<Vector2D> = Vector2D.OMNI_DIRS
            return findSliderActions(index, state, unitDirections)
        }

        /**
         * Find king actions
         *
         * In this variant, king can move into check
         * TODO: technically a slider with max distance of 1
         *
         * @param index
         * @param state
         * @return
         */
        fun findKingActions(index: Int, state: BoardState): List<Int> {
            val unitDirections: List<Vector2D> = Vector2D.OMNI_DIRS
            val initialPos = Board.getPositionFromIndex(index)
            val thisChar: Char = state.board.board[index]
            val possibleEndIndices: MutableList<Int> = mutableListOf()

            for (dir in unitDirections) {
                // look at each end position
                val endPos = initialPos + dir
                if (Board.positionInsideBounds(endPos)) {

                    // if an ally, it is blocked
                    val thatChar: Char = state.board.getPieceChar(endPos)
                    if (PieceType.isAlly(thisChar, thatChar)) {
                        continue
                    }
                    possibleEndIndices.add(Board.getIndexFromPosition(endPos))

                }
            }

            return possibleEndIndices
        }

        /**
         * Find horse actions
         *
         * // Technically, could fit under modified findSliderActions() but use this function for now
         * // Lots of reusable code, refactor later after verifying it works
         *
         * @param index
         * @param state
         * @return
         */
        fun findHorseActions(index: Int, state: BoardState): List<Int> {
            val checkDirs: List<Vector2D> = Vector2D.STRAIGHTS
            val initialPos = Board.getPositionFromIndex(index)
            val thisChar: Char = state.board.board[index]
            val possibleEndIndices: MutableList<Int> = mutableListOf()

            for (dir in checkDirs) {
                // look at each end position
                val endPos = initialPos + dir

                if (Board.positionInsideBounds(endPos)) {

                    // if not an empty space, this direction is blocked
                    val aheadChar: Char = state.board.getPieceChar(endPos)
                    if (aheadChar != ' ') {
                        continue
                    }
                    // this direction is not blocked, so look at diagonals
                    // horse jump vectors
                    val jumpVectors: List<Vector2D> = Vector2D.getDirectionDiagonals(dir)
                    for (v in jumpVectors) {
                        val finalJumpPosition = endPos + v

                        if (Board.positionInsideBounds(finalJumpPosition)) {
                            val thatChar: Char = state.board.getPieceChar(finalJumpPosition)
                            if (PieceType.isAlly(thisChar, thatChar)) {
                                continue
                            }
                            // we can do an "L" shaped jump
                            possibleEndIndices.add(Board.getIndexFromPosition(finalJumpPosition))
                        }
                    }


                }
            }

            return possibleEndIndices
        }

        /**
         * Find scholar actions
         *
         * Hardcoded implementation, assumes a 6x6 board, and starts at Vector(3,0)
         *
         * @param index
         * @param state
         * @return
         */
        fun findScholarActions(index: Int, state: BoardState): List<Int> {
            // Scholars can move in any direction, but must be within a 2 X 2 region
            // hardcode the region for now
            // assumptions: always 2x2 region, Vector(3,0) is starting position
            val assumedStart = Vector2D(3, 0)
            val north = assumedStart + Vector2D.NORTH
            val west = assumedStart + Vector2D.WEST
            val nw = assumedStart + Vector2D.NW
            val possibleEndIndices: MutableList<Int> = mutableListOf()
            val thisChar = state.board.board[index]

            val allowedPositions: List<Vector2D> = listOf(assumedStart, north, west, nw)
            for (p in allowedPositions) {
                // if this position has an ally, not allowed to move here
                val thatChar: Char = state.board.getPieceChar(p)

                if (PieceType.isAlly(thisChar, thatChar)) {
                    continue
                }
                // legal action
                possibleEndIndices.add(Board.getIndexFromPosition(p))
            }
            return possibleEndIndices
        }

        /**
         * Find General actions
         *
         * Hardcoded implementation, assumes a 6x6 board, and starts within specified 2x2 allowed positions
         * uses same code as Scholar
         *
         * @param index
         * @param state
         * @return
         */
        fun findGeneralActions(index: Int, state: BoardState): List<Int> = findScholarActions(index, state)

        /**
         * Find cannon actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findCannonActions(index: Int, state: BoardState): List<Int> {
            // look at line of sight, if empty is legal move
            // if see first enemy, not allowed to move there unless second piece in that line of sight is an enemy

            val unitDirections = Vector2D.STRAIGHTS
            val initialPos = Board.getPositionFromIndex(index)
            val thisChar: Char = state.board.board[index]
            val possibleEndIndices: MutableList<Int> = mutableListOf()

            // for each line of sight
            for (dir in unitDirections) {
                // look at each end position
                var endPos = initialPos + dir

                // first, find all empty spaces within this line of sight
                while (Board.positionInsideBounds(endPos)) {
                    // if empty space, then add it in
                    val thatChar = state.board.getPieceChar(endPos)
                    if (thatChar != ' ') break
                    possibleEndIndices.add(Board.getIndexFromPosition(endPos))
                    endPos += dir

                }
                // second, look for second piece beyond the non-empty space
                endPos += dir
                while (Board.positionInsideBounds(endPos)) {
                    //if empty keep searching and don't add, if enemy stop and add
                    val destChar = state.board.getPieceChar(endPos)
                    if (PieceType.isAlly(thisChar, destChar)) {
                        // is ally, can't attack it so STOP
                        break
                    }
                    if (PieceType.isEnemy(thisChar, destChar)) {
                        // is enemy, can attack it, and STOP
                        possibleEndIndices.add(Board.getIndexFromPosition(endPos))
                        break
                    }

                    endPos += dir
                }

            }
            return possibleEndIndices
        }
    }



}