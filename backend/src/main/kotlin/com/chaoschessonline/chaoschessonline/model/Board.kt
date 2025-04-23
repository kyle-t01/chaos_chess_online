package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D
import kotlin.reflect.jvm.internal.impl.incremental.components.Position


/**
 * Board
 *
 * @property board
 * @constructor Create empty Board
 */
data class Board(
    val board: Array<Char> = arrayOf()
)
{



    /**
     * Get piece char (assume position is not out of bounds)
     *
     * @param position
     * @return
     */
    fun getPieceChar(position: Vector2D): Char {
        assert(positionInsideBounds(position))
        return board[getIndexFromPosition(position)]
    }

    /**
     * Apply action, assumes action is already valid
     *
     * @param action
     * @return a new Board from applied action
     */
    fun applyAction(action: Action): Board {
        val from:Int = getIndexFromPosition(action.from)
        val to:Int = getIndexFromPosition(action.to)
        // simply replace the dest/to piece with src/from piece
        val newBoardArr = board.copyOf()
        newBoardArr[to] = newBoardArr[from]
        newBoardArr[from] = ' '
        return Board(newBoardArr)
    }

    /**
     * Find attack direction of pos
     *
     * assumes that pos is valid and within bounds
     *
     * @param pos
     * @return
     */
    fun findAttackDirectionOfPos(pos: Vector2D): Vector2D {
        return PieceType.findAttackDirection(getPieceChar(pos))
    }

    /**
     * Is enemy pos
     *
     * @param src: Vector2D
     * @param dest: Vector2D
     * @return
     */
    fun isEnemyPos(src:Vector2D, dest:Vector2D):Boolean {
        require(positionInsideBounds(src) && positionInsideBounds(dest)) {"ERROR: src or dest must be within bounds"}
        return isEnemyPos(Board.getIndexFromPosition(src), Board.getIndexFromPosition(dest))
    }

    /**
     * Is enemy pos
     *
     * @param src
     * @param dest
     * @return
     */
    fun isEnemyPos(src:Int, dest:Int): Boolean {
        return PieceType.isEnemy(board[src], board[dest])
    }

    /**
     * Is ally pos
     *
     * @param src
     * @param dest
     * @return
     */
    fun isAllyPos(src: Vector2D, dest:Vector2D):Boolean {
        require(positionInsideBounds(src) && positionInsideBounds(dest)) {"ERROR: src or dest must be within bounds"}
        return isAllyPos(Board.getIndexFromPosition(src), Board.getIndexFromPosition(dest))
    }

    /**
     * Is ally pos
     *
     * @param src
     * @param dest
     * @return
     */
    fun isAllyPos(src: Int, dest:Int):Boolean {
        return PieceType.isAlly(board[src], board[dest])
    }

    /**
     * Is empty pos
     *
     * @param dest
     * @return
     */
    fun isEmptyPos(dest:Vector2D):Boolean {
        require(positionInsideBounds(dest))
        return isEmptyPos(Board.getIndexFromPosition(dest))
    }

    /**
     * Is empty pos
     *
     * @param dest
     * @return
     */
    fun isEmptyPos(dest:Int):Boolean {
        return board[dest] == ' '
    }

    companion object {
        fun defaultBoard():Board {
            val size = DEFAULT_DIMENSION.col * DEFAULT_DIMENSION.row
            val array:Array<Char> = Array(size){' '}
            // insert xiangqi pieces to board (south player)
            for (pos in XIANGQI_PIECES_BOTTOM_HALF.keys) {
                val i = getIndexFromPosition(pos)
                val c = PieceType.toChar(XIANGQI_PIECES_BOTTOM_HALF[pos]!!, Vector2D.NORTH)
                array[i] = c
            }
            // insert chess pieces to board (north player)
            for (pos in CHESS_PIECES_TOP_HALF.keys) {
                val i = getIndexFromPosition(pos)
                val c = PieceType.toChar(CHESS_PIECES_TOP_HALF[pos]!!, Vector2D.SOUTH)
                array[i] = c
            }
            return Board(array)
        }

        val XIANGQI_PIECES_BOTTOM_HALF: Map<Vector2D, PieceType> = mapOf(
            Vector2D(1,0) to PieceType.HORSE,
            Vector2D(4,0) to PieceType.HORSE,
            Vector2D(3,0) to PieceType.SCHOLAR,
            Vector2D(2,0) to PieceType.GENERAL,
            Vector2D(1,1) to PieceType.CANNON,
            Vector2D(4,1) to PieceType.CANNON,
            Vector2D(0,2) to PieceType.FOOT_SOLDIER,
            Vector2D(2,2) to PieceType.FOOT_SOLDIER,
            Vector2D(3,2) to PieceType.FOOT_SOLDIER,
            Vector2D(5,2) to PieceType.FOOT_SOLDIER,
        )

        val CHESS_PIECES_TOP_HALF: Map<Vector2D, PieceType> = mapOf(
            Vector2D(0,5) to PieceType.ROOK,
            Vector2D(1,5) to PieceType.BISHOP,
            Vector2D(3,5) to PieceType.QUEEN,
            Vector2D(2,5) to PieceType.KING,
            Vector2D(4,5) to PieceType.BISHOP,
            Vector2D(5,5) to PieceType.ROOK,
            Vector2D(0,4) to PieceType.PAWN,
            Vector2D(1,4) to PieceType.PAWN,
            Vector2D(2,4) to PieceType.PAWN,
            Vector2D(3,4) to PieceType.PAWN,
            Vector2D(4,4) to PieceType.PAWN,
            Vector2D(5,4) to PieceType.PAWN,
        )

        val DEFAULT_DIMENSION = Vector2D(6,6)
        val DEFAULT_SIZE = DEFAULT_DIMENSION.col * DEFAULT_DIMENSION.row

        fun getIndexFromPosition(position: Vector2D): Int {
            require(positionInsideBounds(position)) {"INCORRECT USAGE: position must be within board"}
            return position.col + position.row * DEFAULT_DIMENSION.col
        }

        fun getPositionFromIndex(index: Int): Vector2D {
            val col:Int = index % DEFAULT_DIMENSION.col
            val row:Int =  index / DEFAULT_DIMENSION.row
            return Vector2D(col, row)
        }

        /**
         * Get index from applying vector (DANGEROUS)
         *
         * Assumes that vector will not lead it out of bounds
         *
         * @param index
         * @param vector
         * @return result index
         */
        fun getIndexFromApplyingVector(index: Int, vector: Vector2D): Int {
            val currPos = getPositionFromIndex(index)
            val resultantPos = currPos + vector
            val resultantIndex = getIndexFromPosition(resultantPos)
            return resultantIndex
        }

        /**
         * Position inside bounds
         *
         * @param pos
         * @return
         */
        fun positionInsideBounds(pos: Vector2D):Boolean {
            val withinRow:Boolean = pos.row >= 0 && pos.row < Board.DEFAULT_DIMENSION.row
            val withinCol:Boolean = pos.col >= 0 && pos.col < Board.DEFAULT_DIMENSION.col
            return withinCol && withinRow
        }

        fun isPositionInNorth(pos: Vector2D):Boolean {
            val rowsPerSide = Board.DEFAULT_DIMENSION.row / 2
            return pos.row + 1 > rowsPerSide
        }
    }


}
