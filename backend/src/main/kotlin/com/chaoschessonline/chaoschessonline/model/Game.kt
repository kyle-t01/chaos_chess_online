package com.chaoschessonline.chaoschessonline.model

class Game()
{
    private var currentState:BoardState = BoardState.defaultBoardState()
    private val currentPlayers: MutableList<Player> = mutableListOf()

    fun printGameState() = println(currentState)



}