package com.chaoschessonline.chaoschessonline.util

import kotlin.math.abs

/**
 * A position on a board
 *
 * Immutable position represented by row and col
 *
 * @property row
 * @property col
 */
data class Pos(val row: Int, val col: Int)
{
    // operations on positions
    operator fun plus(other: Pos): Pos = Pos(row + other.row, col + other.col)
    operator fun minus(other: Pos): Pos = Pos(row - other.row, col - other.col)
    operator fun times(scalar: Int): Pos = Pos(scalar * row, scalar * col)

    /**
     * Distance to another Pos
     *
     * Manhattan distance between this and other Pos
     *
     * @param other
     * @return Manhattan distance
     */
    fun distanceTo(other: Pos): Int {
        return (other - this).findMagnitude()
    }

    /**
     * Find magnitude
     *
     * Find magnitude (manhattan distance) from Pos(0,0)
     *
     * @return magnitude of a position
     */
    private fun findMagnitude() : Int {
        return abs(this.row) + abs(this.col)
    }


}

