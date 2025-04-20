package com.chaoschessonline.chaoschessonline.model

import org.springframework.web.socket.WebSocketSession
// should contain all active users
class Lobby (
    private val players:MutableMap<WebSocketSession, Player> = mutableMapOf(),
    private var isGameStarted: Boolean = false
)
{


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

    fun getPlayers():List<Player> {
        return players.values.toList()
    }

    fun addToPlayers(session: WebSocketSession, player: Player) {
        // associate the session with this player
        players[session] = player
        println("$player JOINED the Lobby! [${players.size} players]")
        return
    }

    fun startGame() {
        println("Game has officially started.")
        isGameStarted = true
        // start the game
    }

    fun endGame() {
        println("Game has been terminated.")
        isGameStarted = false
        // reset Game

    }

    // attempt to apply a player's move



}