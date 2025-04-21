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
    }


}
