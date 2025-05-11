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
    val board: Array<Char> = arrayOf(),
    val dimension: Vector2D
)
{
     /**
     * Get piece char (assume position is not out of bounds)
     *
     * @param position
     * @return
     */
    fun getPieceChar(position: Vector2D): Char {
        return board[position.toIndex(dimension)]
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
        return applyAction(from, to)
    }

    /**
     * Apply action
     *
     * @param from
     * @param to
     * @return
     */
    private fun applyAction(from: Int, to: Int): Board {
        val newBoardArr = board.copyOf()
        newBoardArr[to] = newBoardArr[from]
        newBoardArr[from] = ' '
        //println("(1) Moving [${from}] ${board[from]} TOWARDS [${to}] ${board[to]}..." )
        //println("(2) Result [${from}] ${newBoardArr[from]} :  [${to}] ${newBoardArr[to]}")
        return Board(newBoardArr, dimension)
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
        return isEnemyIdx(getIndexFromPosition(src), getIndexFromPosition(dest))
    }

    /**
     * Is enemy pos
     *
     * @param src
     * @param dest
     * @return
     */
    private fun isEnemyIdx(src:Int, dest:Int): Boolean {
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
        return isAllyIdx(getIndexFromPosition(src), getIndexFromPosition(dest))
    }

    /**
     * Is ally pos
     *
     * @param src
     * @param dest
     * @return
     */
    private fun isAllyIdx(src: Int, dest:Int):Boolean {
        return PieceType.isAlly(board[src], board[dest])
    }

    /**
     * Is empty pos
     *
     * @param dest
     * @return
     */
    fun isEmptyPos(pos:Vector2D):Boolean {
        return getPieceChar(pos) == ' '
    }

    /**
     * Find positions of atkDir that represents an attacking direction
     *
     * @param atkDir
     * @return
     */
    fun findPositionsOfAtkDir(atkDir: Vector2D): List<Vector2D> {
        val pos: MutableList<Vector2D> = mutableListOf()
        var idx = 0
        while (idx < dimension.findSize()) {
            val c = board[idx]
            if (c!= ' ' && PieceType.isPieceOfAttacker(c, atkDir)) {
                // piece of attacking direction, so add it to the list
                pos.add(Vector2D.fromIndex(idx, dimension))
            }

            idx++
        }
        return pos
    }

    /**
     * Is leader in supplied positionsList
     *
     * Linear search
     *
     * @param positionList
     * @return
     */
    fun isLeaderInPositions(positionList: List<Vector2D>): Boolean {
        return findLeaderFromPositions(positionList) != null
    }


    fun isLeaderInPos(pos: Vector2D): Boolean {
        return PieceType.isLeaderPiece(this.getPieceChar(pos))
    }

    /**
     * Filter Leader Position (ASSUMING ONLY ONE LEADER PIECE)
     *
     * @param positionList
     * @return
     */
    fun findLeaderFromPositions(positionList: List<Vector2D>): Vector2D? {
        // we are always assuming that there is only ONE leader
        // do linear search
        val leaderPos = positionList.find { isLeaderInPos(it) }
        return leaderPos
    }

    /**
     * Find sum of piece scores added together
     *
     * @return score
     */
    fun findAllPiecesScore(): Double {
        var score = 0.0
        for (c in board) {
            score += PieceType.getScore(c)
        }
        return score
    }

    /**
     * Pretty print
     *
     */
    fun prettyPrint(){
        println("######")
        var c = 0
        var r = dimension.row - 1
        while (r >= 0) {
            c = 0
            while (c < dimension.col) {
                val i = getIndexFromPosition(Vector2D(c, r))
                if (board[i] == ' ') {
                    print('.')
                } else {
                    print(board[i])
                }
                c++
            }
            r--
            println()
        }
        println("######")
    }

    fun getIndexFromPosition(position: Vector2D): Int {
        //require(positionInsideBounds(position)) {"INCORRECT USAGE: position must be within board"}
        return position.col + position.row * dimension.col
    }

    fun getPositionFromIndex(index: Int): Vector2D {
        val col:Int = index % dimension.col
        val row:Int =  index / dimension.row
        return Vector2D(col, row)
    }

    /**
     * Position inside bounds
     *
     * @param pos
     * @return
     */
    fun positionInsideBounds(pos: Vector2D):Boolean {
        return pos.withinBounds(dimension)
    }

    companion object {
        fun defaultBoard():Board {
            val boardDim = DEFAULT_6X6_DIMENSION
            val numPieces = boardDim.findSize()
            val array:Array<Char> = Array(numPieces){' '}
            // insert xiangqi pieces to board (south player)
            for (pos in XIANGQI_PIECES_BOTTOM_HALF.keys) {
                val i = pos.toIndex(boardDim)
                val c = PieceType.toChar(XIANGQI_PIECES_BOTTOM_HALF[pos]!!, Vector2D.NORTH)
                array[i] = c
            }
            // insert chess pieces to board (north player)
            for (pos in CHESS_PIECES_TOP_HALF.keys) {
                val i = pos.toIndex(boardDim)
                val c = PieceType.toChar(CHESS_PIECES_TOP_HALF[pos]!!, Vector2D.SOUTH)
                array[i] = c
            }
            return Board(array, boardDim)
        }

        fun fromString(str: String, dim: Vector2D): Board {
            val arr = str.split(",").map { it.trim().firstOrNull() ?: ' ' }.toTypedArray()
            return Board(arr, dim)
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

        private val DEFAULT_6X6_DIMENSION = Vector2D(6,6)



    }


}
