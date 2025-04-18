package com.chaoschessonline.chaoschessonline.util

import kotlin.math.abs

/**
 * A position vector on a board
 *
 * Immutable position vector represented by row and col
 *
 * @property row
 * @property col
 */
data class Vector2D(val row: Int, val col: Int)
{
    // operations on positions
    operator fun plus(other: Vector2D): Vector2D = Vector2D(row + other.row, col + other.col)
    operator fun minus(other: Vector2D): Vector2D = Vector2D(row - other.row, col - other.col)
    operator fun times(scalar: Int): Vector2D = Vector2D(scalar * row, scalar * col)

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
     * Rotate position anticlockwise by 90 degrees
     *
     * Rotate position (vector) around Vector2D(0,0)
     *
     * @return rotated Vector2D
     */
    private fun rotate(): Vector2D {
        return Vector2D(-col, row)
    }

    /**
     * Reflect the row component of Vector2D
     *
     * @return row-reflected Vector2D
     */
    fun reflectRow(): Vector2D {
        return Vector2D(-row, col)
    }


    companion object {
        // unit directions
        val NORTH = Vector2D(1, 0)
        val SOUTH = Vector2D(-1,0)
        val EAST = Vector2D(0,1)
        val WEST = Vector2D(0, -1)
        val NE = Vector2D(1,1)
        val NW = Vector2D(1, -1)
        val SE = Vector2D(-1, 1)
        val SW = Vector2D(-1, -1)

    }
}

