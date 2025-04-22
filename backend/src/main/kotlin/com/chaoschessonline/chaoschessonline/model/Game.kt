package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

class Game()
{
    private var currentState:BoardState = BoardState.defaultBoardState()
    private var southPlayer: Player? = null
    private var northPlayer: Player? = null
    private var isStarted:Boolean = false
    private var dimension:Vector2D = Board.DEFAULT_DIMENSION


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

    fun getCurrentState() = currentState

    fun getDimension() = dimension

    // starting and stopping games
    fun start() {
        isStarted = true
    }

    fun end() {
        isStarted = false
    }

    fun isStarted() = isStarted


    fun printGameState() = println(currentState)

    /**
     * Add player to a Game
     *
     * @param player
     * @return added player to game
     */
    fun addPlayer(player: Player?): Boolean {
        if (player == null || isStarted) return false
        // check whether player can be added
        if (player.attackDirection == Player.ATTACK_NORTH && southPlayer == null) {
            southPlayer = player
        } else if (player.attackDirection == Player.ATTACK_SOUTH && northPlayer == null) {
            northPlayer = player
        } else {
            // unimplemented attack direction OR player slot full
            println("error: unknown attack direction or team already full!")
            return false
        }
        return true
    }

    fun removePlayer(player: Player?) {
        // remove corresponding player
        if (southPlayer == player) {
            southPlayer = null
            println("Game: removing south player")
        } else if (northPlayer == player) {
            northPlayer = null
            println("Game: removing north player")
        } else {
            // ignore case where removing non-existing player
            return
        }
        return
    }

}