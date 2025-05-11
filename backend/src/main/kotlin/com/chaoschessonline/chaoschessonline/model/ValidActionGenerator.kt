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
            val actions:List<Action> = findActionsFromPosList(pieces, state)
            // if no leader return empty list
            val ourLeaderPiece = state.findLeaderInPosList(pieces) ?: return listOf()
            // if only leader piece left
            if (pieces.size == 1) return listOf()

            // can we immediately capture enemy leader? if so, take those actions only
            val enemyPieces = state.findCurrentEnemyPieces()
            val enemyLeaderPiece = state.findLeaderInPosList(enemyPieces)
            if (enemyLeaderPiece == null) {
                // no enemyLeader, just return emptyList
                println("enemy leader already dead!!")
                return listOf()
            }

            // find all possible actions
            // if there is no enemy leader to kill just TODO: return actions
            // can we kill enemy leader immediately?
            val actionsThatKillEnemyLeader = actions.filter{it.to == enemyLeaderPiece}
            // yes, can win immediately
            if (actionsThatKillEnemyLeader.isNotEmpty()) return actionsThatKillEnemyLeader
            // no, can't win immediately
            // TODO: wont check whether action will allow leader to be captured
            return actions
        }


        //fun filterActionsThatKillEnemyLeader

        /**
         * Find actions from pos list
         *
         * (prevents inf recursion from using findAllValidActions()
         *
         * @param positions
         * @param state
         * @return
         */
        private fun findActionsFromPosList(positions: List<Vector2D>, state: BoardState): List<Action> {
            val actions:MutableList<Action> = mutableListOf()
            for (srcPos in positions) {
                val validDests = findPossibleActionsForPosition(srcPos, state)
                for (destPos in validDests) {
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
        fun findPossibleActionsForPosition(pos: Vector2D, state: BoardState): List<Vector2D> {
            // assume that index always within bounds, and called by moving player
            val c = state.board.getPieceChar(pos)
            val pieceChar = c.uppercaseChar()
            var possibleEndPos: List<Vector2D> = listOf();
            when (pieceChar) {
                'P' -> {
                    possibleEndPos = findPawnActions(pos, state)
                }

                'R' -> {
                    possibleEndPos = findRookActions(pos, state)
                }

                'B' -> {
                    possibleEndPos = findBishopActions(pos, state)
                }

                'Q' -> {
                    possibleEndPos = findQueenActions(pos, state)
                }

                'K' -> {
                    possibleEndPos = findKingActions(pos, state)
                }
                // xiangqi pieces
                'Z' -> {
                    possibleEndPos = findFootSoldierActions(pos, state)
                }

                'M' -> {
                    possibleEndPos = findHorseActions(pos, state)
                }

                'S' -> {
                    possibleEndPos = findScholarActions(pos, state)
                }

                'G' -> {
                    possibleEndPos = findGeneralActions(pos, state)
                }

                'C' -> {
                    possibleEndPos = findCannonActions(pos, state)
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
            return possibleEndPos
        }



        /**
         * Find pawn actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findPawnActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            // unit vector for attack direction

            val unitDirections = Vector2D.STRAIGHTS
            val initialPos = pos
            val board = state.board
            val possibleEndPos: MutableList<Vector2D> = mutableListOf()
            val atkDir = PieceType.findAttackDirection(board.getPieceChar(initialPos))
            // look at possible movement
            var dist = 1
            while (dist <= 2) {
                // check some end positions
                val endPos = initialPos + (atkDir * dist)
                if (!board.positionInsideBounds(endPos)) break;

                // if end position is empty, then can move there
                if (!board.isEmptyPos(endPos)) break
                possibleEndPos.add(endPos)
                dist += 1
            }

            // look at possible attacks
            val endPosNW = initialPos + (Vector2D.NW * atkDir.row)
            val endPosNE = initialPos + (Vector2D.NE * atkDir.row)
            val attackPositions: List<Vector2D> = listOf(endPosNE, endPosNW)

            for (atk in attackPositions) {
                if (!board.positionInsideBounds(atk)) continue
                // within bounds, so check whether an enemy
                if (!board.isEnemyPos(initialPos, atk)) continue
                // is an enemy, so valid attack action
                possibleEndPos.add(atk)

            }

            return possibleEndPos

        }

        /**
         * Find foot soldier actions
         *
         * @param index
         * @param state
         * @return
         */
        fun findFootSoldierActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            // unit vector for attack direction

            val initialPos = pos

            val possibleEndPos: MutableList<Vector2D> = mutableListOf()
            val board = state.board
            val attackDirection = PieceType.findAttackDirection(board.getPieceChar(initialPos))
            // before no mans land: move forward, attack forward
            // after no mans land: move and attack UP, LEFT and RIGHT

            val possibleActionVectors: List<Vector2D> = listOf(attackDirection, Vector2D.EAST, Vector2D.WEST)

            for (dir in possibleActionVectors) {
                val endPos: Vector2D = initialPos + dir
                if (!board.positionInsideBounds(endPos)) continue
                if (board.isAllyPos(initialPos, endPos)) continue
                // either an enemy or empty space
                possibleEndPos.add(endPos)

                // if we have NOT crossed half of the board. then ignore other moves
                val whichHalf = endPos.findWhichHalf(board.dimension)
                // if we are attacking NORTH, crossed half when in NORTH (vice versa for SOUTH)
                val crossedHalf = (whichHalf ==  attackDirection)
                if (!crossedHalf) break
            }
            return possibleEndPos
        }

        /**
         * Find slider actions (such as Rook, Chariot, Queen, Bishop)
         *
         * TODO: have maxDistance as a parameter
         *
         * @param pos
         * @param state
         * @param directions
         * @return
         */
        fun findSliderActions(pos: Vector2D, state: BoardState, directions: List<Vector2D>): List<Vector2D> {
            // TODO: index, state, unitMovement, unitAttack, moveDist, attackDist, moveDirs, attackDirs, attackReqs
            val initialPos = pos
            val possibleEndPos: MutableList<Vector2D> = mutableListOf()
            val board = state.board

            // extend in each unit direction, can move there as long as not an ally
            for (dir in directions) {
                // extend in each direction until blocked or out-of-bounds
                var endPos = initialPos + dir
                while (board.positionInsideBounds(endPos)) {
                    // if an ally, it is blocked
                    if (board.isAllyPos(initialPos, endPos)) break
                    // if empty space or will hit first enemy, add pos
                    possibleEndPos.add(endPos)
                    // if hit an enemy, do not look further
                    if (board.isEnemyPos(initialPos, endPos)) break
                    endPos += dir
                }
            }

            return possibleEndPos
        }
        /**
         * Find rook actions
         *
         * @param pos
         * @param state
         * @return
         */
        fun findRookActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            // Rook moves
            val unitDirections: List<Vector2D> = Vector2D.STRAIGHTS
            return findSliderActions(pos, state, unitDirections)
        }

        /**
         * Find bishop actions
         *
         * @param pos
         * @param state
         * @return
         */
        fun findBishopActions(pos: Vector2D, state: BoardState): List<Vector2D> {

            // Bishop moves
            val unitDirections: List<Vector2D> = Vector2D.DIAGONALS
            return findSliderActions(pos, state, unitDirections)
        }

        /**
         * Find queen actions
         *
         * @param pos
         * @param state
         * @return
         */
        fun findQueenActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            val unitDirections: List<Vector2D> = Vector2D.OMNI_DIRS
            return findSliderActions(pos, state, unitDirections)
        }

        /**
         * Find king actions
         *
         * In this variant, king can move into check
         * TODO: technically a slider with max distance of 1
         *
         * @param pos
         * @param state
         * @return
         */
        fun findKingActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            val unitDirections: List<Vector2D> = Vector2D.OMNI_DIRS
            val initialPos = pos
            val board = state.board
            val possibleEndPos:MutableList<Vector2D> = mutableListOf()

            for (dir in unitDirections) {
                // look at each end position
                val endPos = initialPos + dir
                if (!board.positionInsideBounds(endPos)) continue

                // if an ally, it is blocked
                if (board.isAllyPos(initialPos, endPos)) continue
                possibleEndPos.add(endPos)

            }
            return possibleEndPos
        }


        /**
         * Find horse actions
         *
         * @param pos
         * @param state
         * @return
         */
        fun findHorseActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            val checkDirs: List<Vector2D> = Vector2D.STRAIGHTS
            val initialPos = pos
            val possibleEndPos: MutableList<Vector2D> = mutableListOf()
            val board = state.board

            for (dir in checkDirs) {
                // look at each end position
                val endPos = initialPos + dir
                if (!board.positionInsideBounds(endPos)) continue
                // if not an empty space, this direction is blocked
                if (!board.isEmptyPos(endPos)) continue
                // this direction is not blocked, so look at diagonals
                // horse jump vectors
                val jumpVectors: List<Vector2D> = Vector2D.getDirectionDiagonals(dir)
                for (v in jumpVectors) {
                    val finalJumpPosition = endPos + v

                    if (!board.positionInsideBounds(finalJumpPosition)) continue
                    // destination is an ally
                    if (board.isAllyPos(initialPos, finalJumpPosition)) continue

                    // we can do an "L" shaped jump
                    possibleEndPos.add(finalJumpPosition)
                    }
                }
            return possibleEndPos
        }

        /**
         * Find scholar actions
         *
         * Hardcoded implementation, assumes a 6x6 board, and starts at Vector(3,0)
         *
         * @param pos
         * @param state
         * @return
         */
        fun findScholarActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            // Scholars can move in any direction once, but must be within a 2x2 or 3x3 box in their half
            val initialPos = pos
            val board = state.board
            val possibleEndPos:MutableList<Vector2D> = mutableListOf()
            val startLocation = state.attackingDirection.reflectRow()
            // find middle third
            // ie: if width [.|.|.|.] take middle two, if width odd take middle three
            val allowedPositions: List<Vector2D> = Vector2D.findMiddleRanges(board.dimension,startLocation)
            // middleBoxPosList is guaranteed to be within bounds
            val middleBoxPosList = allowedPositions.filter { board.positionInsideBounds(it) }

            val unitDirections = Vector2D.OMNI_DIRS
            for (dir in unitDirections) {
                // look at each end position
                val endPos = initialPos + dir
                // is that within middleBoxPositionsList?
                if (!middleBoxPosList.contains(endPos)) continue
                // if an ally, it is blocked
                if (board.isAllyPos(initialPos, endPos)) continue
                possibleEndPos.add(endPos)
            }
            return possibleEndPos
        }

        /**
         * Find general actions
         *
         * @param pos
         * @param state
         * @return
         */
        fun findGeneralActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            // Generals can move in any direction once, but must be within a 2x2 or 3x3 box in their half
            val initialPos = pos
            val board = state.board
            val possibleEndPos:MutableList<Vector2D> = mutableListOf()
            val startLocation = state.attackingDirection.reflectRow()
            // find middle third
            // ie: if width [.|.|.|.] take middle two, if width odd take middle three
            val allowedPositions: List<Vector2D> = Vector2D.findMiddleRanges(board.dimension,startLocation)
            // middleBoxPosList is guaranteed to be within bounds
            val middleBoxPosList = allowedPositions.filter { board.positionInsideBounds(it) }

            val unitDirections = Vector2D.OMNI_DIRS
            for (dir in unitDirections) {
                // look at each end position
                val endPos = initialPos + dir
                // is that within middleBoxPositionsList?
                if (!middleBoxPosList.contains(endPos)) continue
                // if an ally, it is blocked
                if (board.isAllyPos(initialPos, endPos)) continue
                possibleEndPos.add(endPos)
            }
            return possibleEndPos
        }

        /**
         * Find cannon actions
         *
         * @param pos
         * @param state
         * @return
         */
        fun findCannonActions(pos: Vector2D, state: BoardState): List<Vector2D> {
            // look at line of sight, if empty is legal move
            // if see first enemy, not allowed to move there unless second piece in that line of sight is an enemy

            val unitDirections = Vector2D.STRAIGHTS
            val initialPos = pos
            val board = state.board
            val possibleEndPos: MutableList<Vector2D> = mutableListOf()


            // for each line of sight
            for (dir in unitDirections) {
                // look at each end position
                var endPos = initialPos + dir
                // first, find all empty spaces within this line of sight
                while (board.positionInsideBounds(endPos)) {
                    // if empty space, then add it in
                    if (!board.isEmptyPos(endPos)) break
                    possibleEndPos.add(endPos)
                    endPos += dir

                }
                // second, look for second piece beyond the non-empty space
                endPos += dir
                while (board.positionInsideBounds(endPos)) {
                    // can't attack on ally, STOP
                    if (board.isAllyPos(initialPos, endPos)) break

                    // can attack the first enemy, STOP
                    if (board.isEnemyPos(initialPos, endPos)) {
                        possibleEndPos.add(endPos)
                        break
                    }
                    // if empty space, continue
                    endPos += dir
                }
            }
            return possibleEndPos
        }
    }



}