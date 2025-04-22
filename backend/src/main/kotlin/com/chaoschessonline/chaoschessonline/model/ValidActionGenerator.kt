package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.model.*
import com.chaoschessonline.chaoschessonline.util.Vector2D

class ValidActionGenerator {

    companion object {
        /**
         * from char, get attackDirection, and Piece
         * actions = moves + attacks
         *
         *
         */
        fun findPossibleActionsForIndex(index: Int, state:BoardState):List<Int> {
            // assume that index always within bounds, and called by moving player
            val c = state.board.board[index]
            val pieceChar = c.uppercaseChar()
            var possibleEndIndices:List<Int> = listOf();
            when(pieceChar){
                'P' -> {possibleEndIndices = findPawnActions(index, state)}
                'R' -> {possibleEndIndices = findRookActions(index, state)}
                'Z' -> {possibleEndIndices = findFootSoldierActions(index, state)}

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

        fun findPawnActions(index: Int, state: BoardState): List<Int> {
            // unit vector for attack direction
            val attackDirection = state.attackingDirection
            val initialPos = Board.getPositionFromIndex(index)
            val thisChar:Char = state.board.board[index]
            val possibleEndIndices:MutableList<Int> = mutableListOf()

            // look at possible movement
            var dist = 1
            while (dist <= 2) {
                // check some end positions
                val endPos = initialPos + (attackDirection * dist)
                if (!Board.positionInsideBounds(endPos)) continue
                val endIndex = Board.getIndexFromPosition(endPos)

                // if end position is empty, then can move there
                if (state.board.board[endIndex] == ' ') {
                    possibleEndIndices.add(endIndex)
                }
                dist +=1
            }

            // look at possible attacks
            val endPosNW = Vector2D.NW * attackDirection.row
            val endPosNE = Vector2D.NE * attackDirection.row
            val attackPositions:List<Vector2D> = listOf(endPosNE, endPosNW)

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

        fun findFootSoldierActions(index: Int, state: BoardState): List<Int> {
            // unit vector for attack direction
            val attackDirection = state.attackingDirection
            val initialPos = Board.getPositionFromIndex(index)
            val thisChar:Char = state.board.board[index]
            val possibleEndIndices:MutableList<Int> = mutableListOf()

            // before no mans land: move forward, attack forward
            // after no mans land: move and attack UP, LEFT and RIGHT

            val possibleActionVectors:List<Vector2D> = listOf(attackDirection, Vector2D.EAST, Vector2D.WEST)

            for (dir in possibleActionVectors) {
                val endPos: Vector2D = initialPos + dir
                if(!Board.positionInsideBounds(endPos)) continue
                // if endPos has an ally, not allowed to go there
                val thatChar:Char = state.board.getPieceChar(endPos)
                if(PieceType.isAlly(thisChar, thatChar)) continue
                possibleEndIndices.add(Board.getIndexFromPosition(endPos))

                // if we have NOT crossed half of the board. then ignore other moves
                if (!Board.isPositionInNorth(initialPos)) break
            }
            return possibleEndIndices
        }

        fun findRookActions(index: Int, state: BoardState):List<Int> {
            val attackDirection = state.attackingDirection
            val initialPos = Board.getPositionFromIndex(index)
            val thisChar:Char = state.board.board[index]
            println("THIS CHAR is $thisChar")
            val possibleEndIndices:MutableList<Int> = mutableListOf()

            // Rook moves
            val unitDirections:List<Vector2D> = listOf(Vector2D.NORTH, Vector2D.SOUTH, Vector2D.EAST, Vector2D.WEST)

            // extend in each unit direction, can move there as long as not an ally
            for (dir in unitDirections) {
                // extend in each direction until blocked or out-of-bounds
                var endPos = initialPos + dir
                while (Board.positionInsideBounds(endPos)) {

                    // if an ally, it is blocked
                    val thatChar:Char = state.board.getPieceChar(endPos)
                    println("LOOKING AT $endPos which contains $thatChar")
                    if(PieceType.isAlly(thisChar, thatChar)) {
                        println("ALLY BLOCKED ROOK AT $endPos")
                        break
                    };
                    possibleEndIndices.add(Board.getIndexFromPosition(endPos))
                    // if hit an enemy, do not look further
                    if(PieceType.isEnemy(thisChar, thatChar)) {
                        println("ENEMY BLOCKED ROOK AT $endPos")
                        break
                    }
                    endPos += dir
                }
            }

            return possibleEndIndices
        }
    }

}