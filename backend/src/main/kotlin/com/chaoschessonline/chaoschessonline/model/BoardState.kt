package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Board state
 *
 * attackingDirection: South player attacks north = Vector2D.NORTH
 *
 * @property parent
 * @property board
 * @property turnNumber
 * @property attackingDirection
 * @constructor Create Board state
 */
data class BoardState(
    val parent: BoardState?,
    val board:Board,
    val turnNumber:Int,
    val attackingDirection: Vector2D
)
{
    var children: List<BoardState> = emptyList()
    var eval:Int = 0

    companion object {
        fun defaultBoardState() = BoardState(null, Board.defaultBoard(), 0, Vector2D.NORTH)
        // awkward work-around for setting board dimensions
        val DIM_6 = Vector2D(6,6)
        val DIM_8 = Vector2D(8,8)
        var dimension = Vector2D(0,0)
    }


}
