package com.chaoschessonline.chaoschessonline.model

import org.springframework.web.socket.WebSocketSession

/**
 * Lobby
 *
 * Contains all actively connected players
 *
 * @property players
 * @property isGameStarted
 * @constructor Create empty Lobby
 */
class Lobby (
    private val players:MutableMap<WebSocketSession, Player> = mutableMapOf(),
    private var isGameStarted: Boolean = false
)
{

    /**
     * Remove player
     *
     * @param session
     */
    fun removePlayer(session: WebSocketSession) {
        val player = players[session]
        println("$player LEFT the Lobby! [${players.size -1} players left...]")
        players.remove(session)

        // if there are no more players, then the game has ended
        if (players.isEmpty()) {
            isGameStarted = false;
        }
        return
    }

    /**
     * Get players
     *
     * @return
     */
    fun getPlayers():List<Player> {
        return players.values.toList()
    }

    /**
     * Add to players
     *
     * @param session
     * @param player
     */
    fun addToPlayers(session: WebSocketSession, player: Player) {
        // associate the session with this player
        players[session] = player
        println("$player JOINED the Lobby! [${players.size} players]")
        return
    }

    /**
     * Start game
     *
     */
    fun startGame() {
        println("Game has officially started.")
        isGameStarted = true
        // start the game
    }

    /**
     * End game
     *
     */
    fun endGame() {
        println("Game has been terminated.")
        isGameStarted = false
        // reset Game

    }

    /**
     * Get is game started
     *
     * @return
     */
    fun getIsGameStarted():Boolean {
        return isGameStarted
    }

    // attempt to apply a player's move



}