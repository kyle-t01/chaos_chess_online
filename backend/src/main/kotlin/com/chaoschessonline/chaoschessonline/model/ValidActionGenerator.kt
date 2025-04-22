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
                //'B' -> findBishopActions()
                //'N' -> findKnightActions()
                /*
                'R' ->
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
                if (!PieceType.isCharEnemy(thisChar, thatChar)) continue
                // is an enemy, so valid attack action
                val endIndex = Board.getIndexFromPosition(pos)
                possibleEndIndices.add(endIndex)

            }

            return possibleEndIndices

        }
    }

}