package com.chaoschessonline.chaoschessonline.model

import org.springframework.web.socket.WebSocketSession

/**
 * Lobby
 *
 * Contains all actively connected players
 *
 * @property players
 * @constructor Create empty Lobby
 */
class Lobby (
    private val players:MutableMap<WebSocketSession, Player> = mutableMapOf(),
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
            println("No players left!")
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
     * Get sessions
     *
     * @return list of sessions
     */
    fun getSessions():List<WebSocketSession> {
        return players.keys.toList()
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
     * Update player (if it exists)
     *
     * @param session
     * @param player
     */
    fun updatePlayer(session: WebSocketSession, player:Player){
        val original:Player? = players[session]
        if(original != null){
            original.name = player.name
            original.attackDirection = player.attackDirection
        }
    }

    /**
     * Find session of player
     *
     * @param player
     * @return session
     */
    fun findSessionOfPlayer(player:Player): WebSocketSession? = players.entries.find { it.value == player }?.key

    /**
     * Find player of session
     *
     * @param session
     * @return player
     */
    fun findPlayerOfSession(session: WebSocketSession): Player? = players[session]

}