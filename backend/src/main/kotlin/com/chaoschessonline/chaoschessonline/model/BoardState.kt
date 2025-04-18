package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Board state
 *
 * @property parent
 * @property board
 * @property turnNumber
 * @property attackVector2D
 * @constructor Create Board state
 */
data class BoardState(
    val parent: BoardState?,
    val board:Board,
    val turnNumber:Int,
    val attackVector2D: Vector2D
)
{
    var children: List<BoardState> = emptyList()
    var eval:Int = 0



}
