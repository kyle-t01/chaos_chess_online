package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

class Game()
{
    private var currentState:BoardState = BoardState.defaultBoardState()
    private var southPlayer: Player? = null
    private var northPlayer: Player? = null

    public var isStarted:Boolean = false

    // getters and setters
    fun getSouthPlayer(): Player? = southPlayer
    fun getNorthPlayer(): Player? = northPlayer

    fun setSouthPlayer(player: Player) {
        this.southPlayer = player
    }

    fun setNorthPlayer(player: Player) {
        this.northPlayer = player
    }

    fun setSouthPlayerAI(player: Player) {
        this.southPlayer = Player("SOUTH AI", true, Vector2D.NORTH)
    }

    fun setNorthPlayerAI(player: Player) {
        this.northPlayer = Player("NORTH AI", true, Vector2D.SOUTH)
    }


    fun printGameState() = println(currentState)



}