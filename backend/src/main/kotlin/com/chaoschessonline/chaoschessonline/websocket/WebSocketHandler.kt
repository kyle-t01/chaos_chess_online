package com.chaoschessonline.chaoschessonline.websocket

import com.chaoschessonline.chaoschessonline.model.*

import kotlinx.coroutines.*

import com.chaoschessonline.chaoschessonline.util.JsonMapper
import com.chaoschessonline.chaoschessonline.util.Vector2D
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler

/**
 * Web socket handler
 *
 * handles websocket messages (
 *
 * @property mapper
 * @constructor Create empty Web socket handler
 */
class WebSocketHandler (private val mapper: JsonMapper) : TextWebSocketHandler(){

    // scope to hold coroutines
    private val gameLoopScope = CoroutineScope(CoroutineName("GameLoopScope"))

    // track gameLoop coroutine
    private var gameLoopJob: Job? = null

    // the lobby
    private val lobby: Lobby = Lobby()

    // the game (ONLY one game is played at a time
    private val  game:Game = Game()

    // remove player from lobby on disconnect
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val player:Player? = lobby.findPlayerOfSession(session)
        lobby.removePlayer(session)
        game.removePlayer(player)
        emitToAllUpdateConnected()

        // when the game has ended, cancel to gameLoopJob
        if (!game.isStarted()) {
            gameLoopJob?.cancel()
            gameLoopJob = null
        }
    }

    // handle game events
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val event: Event = mapper.readTextMessage(message)
        // json = { type: "", data: {} }
        val type = event.type
        val data = event.data

        // print game events sent by players to terminal
        // println("$type: $data")

        when (type) {
            EventType.CONNECT -> {
                val lobbySize = lobby.getPlayers().size
                val sentName = data.toString().trim()
                val name = if (sentName == "") {
                    "GUEST ${lobbySize+1}"
                } else {
                    sentName
                }
                val player = Player(name, false, Vector2D(0,0))
                lobby.addToPlayers(session, player)
                /*
                // did this player join when the game already started?
                if (lobby.getIsGameStarted() || gameLoopJob?.isActive == true) {
                    // then KICK the player
                    println("Kicking ${player.name} from game.")
                    emit(session, Event(EventType.KICKED, ""))
                    return
                }
                */

                // signal to the player, of successful connect
                emit(session,Event(EventType.CONNECTED, game))
                
                // update the lobby of all players
                emitToAllUpdateConnected()
            }
            EventType.JOIN -> {
                // inform everyone of current game state
                emitToAll(Event(EventType.GAME_STATE_UPDATED, game))

                // get the attacking direction of this player
                val dirString:String = data.toString()
                val attackDir:Vector2D = if (dirString == "N") {
                    Player.ATTACK_SOUTH
                } else {
                    Player.ATTACK_NORTH
                }
                // update the player in the lobby
               lobby.updatePlayer(session, Player("", false, attackDir))

                // if the game has started, then ignore JOIN
                if (game.isStarted() || gameLoopJob?.isActive == true) {
                    emit(session,Event(EventType.GAME_STATE_UPDATED, game))
                    return
                }

                // join player to this lobby
                val isAdded = game.addPlayer(lobby.findPlayerOfSession(session))
                if (!isAdded) return


                // tell player game was joined
                emit(session,Event(EventType.JOINED, ""))
                // tell lobby that game state has updated
                emitToAll(Event(EventType.GAME_STATE_UPDATED, game))

            }
            EventType.START -> {
                if (game.isStarted() || gameLoopJob?.isActive == true) {
                    // game already started
                    return
                }

                // emit to everyone the current players and spectators?

                /*
                gameLoopJob = gameLoopScope.launch {
                    println("Launched Coroutine")
                    val answeringDuration:Long = 5000 // durations in ms
                    val revealAnswerDuration:Long = 3000
                    val updateDuration:Long = 1000
                    try {
                        // tell all players game has started!
                        emitToAll(Event(EventType.START, ""))
                        // while we have questions
                        while (!lobby.quiz.isFinished()) {

                            if (lobby.players.isEmpty()) {
                                // if no players, exit co-routine
                                return@launch
                            }
                            // get current question
                            val q = lobby.quiz.getCurrentQ()
                            // send it to all players
                            emitToAll(GameEvent(GameEventType.QUESTION, q))


                            var t:Long = 0
                            // tell players total time allocated for this question
                            emitToAll(GameEvent(GameEventType.TOTAL_TIME, answeringDuration))
                            while(t < answeringDuration) {
                                // give time to players to answer questions
                                emitToAll(GameEvent(GameEventType.TIME, answeringDuration-t))
                                delay(updateDuration)
                                t += updateDuration
                            }
                            // timer has finished
                            emitToAll(GameEvent(GameEventType.TIME, 0))
                            // reveal answer to all players
                            emitToAll(GameEvent(GameEventType.SHOW, q.answers))
                            // give time to players to view answers
                            delay(revealAnswerDuration)
                            // increment the current question index
                            lobby.quiz.currentIndex += 1
                        }
                    } finally {
                        // any time co-routine exits (or when gameLoop needs to end)
                        println("Exiting Coroutine")
                        lobby.endGame()
                        gameLoopJob = null
                        emitToAll(GameEvent(GameEventType.END, ""))
                    }
                }

             */
            }

            EventType.MOVE -> {
                // get move data

                // validate move

                // tell player whether they have moved

                // broadcast state change to everyone

            }
            else -> {
                println("Unexpected Usage of $type !")
            }
        }
    }

    // emit signal to player
    private fun emit(session: WebSocketSession, event: Event) {
        session.sendMessage(mapper.convertToTextMessage(event))
    }

    // emit signal to all players in lobby
    private fun emitToAll(event: Event) {
        for (s in lobby.getSessions()) {
            emit(s, event)
        }
    }

    // helper signal to tell everyone a player CONNECTED
    private fun emitToAllUpdateConnected() {
        emitToAll(Event(EventType.UPDATE_CONNECTED, lobby.getPlayers()))
    }


}

@Configuration
@EnableWebSocket
class WSConfig(private val mapper:JsonMapper): WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(WebSocketHandler(mapper), "/game")
            .setAllowedOrigins("*")
    }
}