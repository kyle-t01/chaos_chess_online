package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Board state
 *
 * attackingDirection: South player attacks north = Vector2D.NORTH
 *
 * @property parent
 * @property board
 * @property turnNumber
 * @property attackingDirection
 * @constructor Create Board state
 */
data class BoardState(
    val parent: BoardState?,
    val board:Board,
    val turnNumber:Int,
    val attackingDirection: Vector2D
)
{
    var children: List<BoardState> = emptyList()
    var eval:Int = 0

    companion object {
        fun defaultBoardState() = BoardState(null, Board.defaultBoard(), 0, Vector2D.NORTH)
        fun northStartBoardState() = BoardState(null, Board.defaultBoard(), 0, Vector2D.SOUTH)
    }

    /**
     * Apply action, and get a new boardstate
     *
     * @param action
     * @return new BoardState
     */
    fun applyAction(action: Action): BoardState {
        // assuming valid action
        val newBoard: Board = board.applyAction(action)
        // make new State from applying action
        val newAttackDir = attackingDirection.reflectRow()
        val newTurnNum = turnNumber + 1
        val parent = this
        return BoardState(parent, newBoard, newTurnNum, newAttackDir)
    }

}
