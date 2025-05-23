package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.ai.NextStateMaker
import com.chaoschessonline.chaoschessonline.util.Vector2D

class Game()
{
    private var currentState:BoardState = BoardState.defaultBoardState()
    private var southPlayer: Player? = null
    private var northPlayer: Player? = null
    private var isStarted:Boolean = false
    private var dimension:Vector2D = Vector2D(6,6)


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
        println("GAME HAS STARTED")
        isStarted = true
    }

    fun end() {
        println("GAME HAS ENDED")
        isStarted = false
        currentState = BoardState.defaultBoardState()
        southPlayer = null
        northPlayer = null
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
            // check whether it is existing player, then change teams
            if (player == northPlayer) {
                northPlayer = null
            }
            southPlayer = player
        } else if (player.attackDirection == Player.ATTACK_SOUTH && northPlayer == null) {
            // check whether it is existing player, then change teams
            if (player == southPlayer) {
                southPlayer = null
            }
            northPlayer = player
        } else {
            // unimplemented attack direction OR player slot full
            println("error: unknown attack direction or team already full!")
            return false
        }
        // start game when both players are there
        if (southPlayer != null && northPlayer!= null) {
            start()
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
            ;
        }
        // if not enough players, end the game
        if (southPlayer == null || northPlayer== null) {
            end()
        }
        return
    }

    /**
     * Is player in game
     *
     * @param player
     * @return
     */
    fun isPlayerInGame(player: Player):Boolean = (player == southPlayer || player == northPlayer)


    /**
     * Apply player action
     *
     * @param player
     * @param action
     * @return whether action was applied
     */
    fun applyPlayerAction(player: Player?, action: Action): Boolean {
        // if player not in game, ignore
        if (player == null || !isPlayerInGame(player)) return false
        // if not the player's turn, ignore
        if (player.attackDirection != currentState.attackingDirection) return false

        // if position piece can't move on this turn, ignore
        if (currentState.isActionOutsideTurn(action)) return false

        // apply action and update current board state
        currentState = currentState.applyAction(action)
        return true
    }

    /**
     * Make next state
     *
     * @return true when a move can be made
     */
    fun makeNextState(): Boolean {
        val newState = NextStateMaker.makeNextState(currentState)
        if (currentState == newState) {
            println("COULD NOT MAKE A RANDOM MOVE for ${currentState.attackingDirection}")
            return false
        }
        // set the new state
        currentState = newState
        return true
    }


}