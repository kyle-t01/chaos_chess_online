package com.chaoschessonline.chaoschessonline.model

enum class EventType {
    JOIN, // player attempted to join a game
    JOINED, // player joined a game, attacking a side, ie: data: Vector2D.NORTH
    CONNECT, // player attempting to connect
    CONNECTED, // tell player connected
    UPDATE_CONNECTED, // tell player current connected players
    START, // player attempted to start game
    STARTED, // tell player game started
    END, // player attempting to end game
    ENDED, // tell player game ended
    CHAT, // player sent a chat
    CHATTED, // tell player someone chatted
    MOVE, // player attempting to make a move
    MOVE_FAILED, // player move could not be applied
    MOVE_UPDATED, // tell player that move updated (the game state)
    GAME_STATE_UPDATED, // tell player that game state has updated
    KICKED, // kick player out of lobby
    LEAVE, // player left
    TIME, // time remaining for a timer
    TOTAL_TIME, // total time allocated for a timer
}