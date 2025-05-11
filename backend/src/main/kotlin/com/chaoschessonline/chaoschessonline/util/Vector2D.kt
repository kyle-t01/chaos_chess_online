package com.chaoschessonline.chaoschessonline.util

import com.chaoschessonline.chaoschessonline.model.Board
import kotlin.math.abs

/**
 * A position vector on a board
 *
 * Immutable position vector represented by row and col
 *
 * @property col
 * @property row
 */
data class Vector2D(val col: Int, val row: Int)
{
    // operations on positions
    operator fun plus(other: Vector2D): Vector2D = Vector2D(col + other.col, row + other.row)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(col - other.col, row - other.row)
    operator fun times(scalar: Int): Vector2D = Vector2D(scalar * col, scalar * row)

    /**
     * Distance to another Vector2D
     *
     * Manhattan distance between this and other Vector2D
     *
     * @param other
     * @return Manhattan distance
     */
    fun distanceTo(other: Vector2D): Int {
        return (other - this).findMagnitude()
    }

    /**
     * Find magnitude
     *
     * Find magnitude (manhattan distance) from Vector2D(0,0)
     *
     * @return magnitude of a position
     */
    private fun findMagnitude() : Int {
        return abs(this.row) + abs(this.col)
    }

    /**
     * Rotate around an anchor Vector2D anticlockwise by 90 degrees
     *
     * @param anchor
     * @return rotated Vector2D
     */
    fun rotateAroundPoint(anchor: Vector2D) : Vector2D {
        return (this - anchor).rotate() + anchor
    }

    /**
     * Find the size of a Vector2D dimension
     *
     * @return size
     */
    fun findSize(): Int {
        return row * col
    }

    /**
     * Within bounds
     *
     * @param dimension
     * @return
     */
    fun withinBounds(dimension: Vector2D): Boolean {
        val withinRow:Boolean = row >= 0 && row < dimension.row
        val withinCol:Boolean = col >= 0 && col < dimension.col
        return withinCol && withinRow
    }

    /**
     * Find which half, either NORTH or SOUTH half a position belongs to
     *
     * @param dimension
     * @return
     */
    fun findWhichHalf(dimension: Vector2D): Vector2D {
        val halfRows = dimension.row / 2 // south = [0,halfRows)
        val noMans = dimension.row % 2 == 1
        if (row < halfRows) return SOUTH
        if (noMans && (row == halfRows)) return ZERO
        if (row > halfRows+1) return NORTH // north = [halfRows, dimRow)
        return ZERO
    }

    /**
     * Rotate position anticlockwise by 90 degrees
     *
     * Rotate position (vector) around Vector2D(0,0)
     *
     * @return rotated Vector2D
     */
    private fun rotate(): Vector2D {
        return Vector2D(-row, col)
    }

    /**
     * Reflect the row component of Vector2D
     *
     * @return row-reflected Vector2D
     */
    fun reflectRow(): Vector2D {
        return Vector2D(col, -row)
    }

    /**
     * To index
     *
     * @param dimension
     * @return
     */
    fun toIndex(dimension: Vector2D): Int {
        return col + row * dimension.col
    }

    companion object {
        // unit directions where (0,0) is located bottom-left
        val NORTH = Vector2D(0, 1)
        val SOUTH = Vector2D(0, -1)
        val EAST  = Vector2D(1, 0)
        val WEST  = Vector2D(-1, 0)

        val NE = Vector2D(1, 1)
        val NW = Vector2D(-1, 1)
        val SE = Vector2D(1, -1)
        val SW = Vector2D(-1, -1)

        val STRAIGHTS = listOf(NORTH, SOUTH, EAST, WEST)
        val DIAGONALS = listOf(NE, NW, SE, SW)
        val OMNI_DIRS = STRAIGHTS + DIAGONALS

        val NORTH_DIAGONALS = listOf(NE, NW)
        val SOUTH_DIAGONALS = listOf(SE, SW)
        val EAST_DIAGONALS = listOf(NE, SE)
        val WEST_DIAGONALS = listOf(NW, SW)

        val ZERO = Vector2D(0,0)

        /**
         * Get direction diagonals (ie NORTH -> NE and NW)
         *
         * @param unitDirection
         * @return
         */
        fun getDirectionDiagonals(unitDirection: Vector2D): List<Vector2D> {
            return when (unitDirection) {
                NORTH -> NORTH_DIAGONALS
                SOUTH -> SOUTH_DIAGONALS
                EAST -> EAST_DIAGONALS
                WEST -> WEST_DIAGONALS
                else -> listOf(Vector2D(0,0))
            }
        }

        /**
         * Create a Vector2D from str
         *
         * assumes that s is in the form: "Int,Int"
         *
         * @param s
         * @return
         */
        fun fromStr(s:String):Vector2D {
            val posStrList = s.split(",")
            require(posStrList.size == 2)
            val colPos = posStrList[0].toInt()
            val rowPos = posStrList[1].toInt()
            return Vector2D(colPos, rowPos)
        }

        /**
         * From index (and given board dimension)
         *
         * @param index
         * @param dimension
         * @return
         */
        fun fromIndex(index: Int, dimension: Vector2D):Vector2D {
            return Vector2D(index % dimension.col, index / dimension.row)
        }

        /**
         * Find middle ranges (the middle 2x2 or 3x3 area of top or bottom of board)
         *
         * @param dimension
         * @param locationVector2D
         * @return
         */
        fun findMiddleRanges(dimension: Vector2D, locationVector2D: Vector2D): List<Vector2D> {
            val ranges:MutableList<Vector2D> = mutableListOf()
            // taking the middle cols
            val mid = dimension.col /2
            val start = mid - 1
            val midCols= if (dimension.col %2 == 0) arrayOf(start, start + 1) else arrayOf(start, start + 1, start + 2)
            // taking rows (from edge of the board)
            var rows:Array<Int> = arrayOf()
            if (locationVector2D == NORTH) {
                // use the top X rows
                val endIdx = dimension.row -1
                rows = if (dimension.row %2 == 0) arrayOf(endIdx,endIdx-1) else arrayOf(endIdx,endIdx-1,endIdx-2)
            }
            if (locationVector2D == SOUTH) {
                // use bottom X rows
                rows = if (dimension.row %2 == 0) arrayOf(0,1) else arrayOf(0,1,2)
            }
            // now combine rows and cols into vectors (that may be out of bounds)
            for (r in rows) {
                for (c in midCols) {
                    ranges.add(Vector2D(c, r))
                }
            }

            return ranges
        }
    }
}

