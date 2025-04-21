package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Player representing a player in the Game
 *
 * @property name
 * @property isAI
 * @property attackDirection
 * @constructor Create empty Player
 */
data class Player (var name: String, val isAI: Boolean, var attackDirection: Vector2D) {
    companion object {
        val DEFAULT_NAME = ""
        val NO_ATTACK_DIRECTION = Vector2D(0,0)
        val ATTACK_NORTH = Vector2D.NORTH
        val ATTACK_SOUTH = Vector2D.SOUTH
    }
}