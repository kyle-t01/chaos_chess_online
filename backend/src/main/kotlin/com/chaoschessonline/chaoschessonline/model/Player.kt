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

}