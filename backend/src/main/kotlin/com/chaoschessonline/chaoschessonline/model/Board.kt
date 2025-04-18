package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Board
 *
 * @property playerPieceList
 * @property enemyPieceList
 * @constructor Create empty Board
 */
data class Board(
    val playerPieceList: Map<Vector2D, PieceType> = mapOf(),
    val enemyPieceList: Map<Vector2D, PieceType> = mapOf()
)
