package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Agent representing a player in the Game
 *
 * @property name
 * @property isAI
 * @property attackDirection
 * @constructor Create empty Agent
 */
data class Agent (val name: String, val isAI: Boolean, var attackDirection: Vector2D) {

}