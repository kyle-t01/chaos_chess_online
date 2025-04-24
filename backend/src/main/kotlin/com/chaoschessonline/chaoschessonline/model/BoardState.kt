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
    var eval:Double = 0.0
    private val attackingPieces:MutableList<Int> = mutableListOf()

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
        // assumes that we are moving piece within turn
        // TODO: remove require
        if(isActionOutsideTurn(action)) {
            println("ApplyAction(): tried to move a piece not on its turn")
            return this
        }

        // assuming valid action
        val newBoard: Board = board.applyAction(action)
        // make new State from applying action
        val newAttackDir = attackingDirection.reflectRow()
        val newTurnNum = turnNumber + 1
        val parent = this

        // setup the newState
        val newState = BoardState(parent, newBoard, newTurnNum, newAttackDir)
        // TODO: newState.attackingPieces = add findCurrentAttackingPieces()


        return newState
    }

    /**
     * Apply action using Int positions
     *
     * @param from
     * @param to
     * @return
     */
    fun applyAction(from : Int, to: Int): BoardState {
        // assuming valid action
        val posFrom = Board.getPositionFromIndex(from)
        val posTo = Board.getPositionFromIndex(to)
        val action = Action(posFrom, posTo)
        return applyAction(action)
    }

    /**
     * Is action outside turn (moving a piece not on its turn)
     *
     *  are we attempting to move pieces that aren't allowed to move yet?
     *
     * @param action
     * @return boolean
     */
    fun isActionOutsideTurn(action: Action): Boolean {

        //println("###isActionOutsideTurn###")
        //println(board)
        //println(attackingDirection)
        //println(action)
        //println("######")

        return board.findAttackDirectionOfPos(action.from) != attackingDirection
    }

    /**
     * Find attacking pieces
     *
     * @param atkDir
     * @return A list of Int indices representing position
     */
    fun findAttackingPieces(atkDir: Vector2D) : List<Int> = board.findAttackerIndices(atkDir)

    /**
     * Find current attacking pieces
     *
     * Use the attacking direction of current moving player
     *
     * @return
     */
    fun findCurrentAttackingPieces() :List<Int> = findAttackingPieces(attackingDirection)

    /**
     * Is terminal state for player
     *
     * terminal state when: no pieces left, no leader left, no valid moves
     *
     * @param atkDir
     * @return
     */
    fun isTerminalStateForPlayer(atkDir: Vector2D): Boolean {
        // terminal state when
        // (0) leader is captured (no leader pieces left)
        // (1) no pieces left
        // (2) no valid moves
        // (3)
        // TODO: when move into new State, generate attacking pieces and defending pieces to speed up calculations
        val pieces:List<Int> = findAttackingPieces(atkDir)
        // no pieces left
        if (pieces.size == 0) return true
        // no leader pieces left
        if (board.isLeaderInPositions(pieces) == false) return true
        // no valid moves
        // for each attacking piece, no legal move
        val validActions = ValidActionGenerator.findActionsOfList(pieces, this)
        if (validActions.size == 0) return true

        return false

        // future implementations
        // (1) moved x amount of turns without a capture
        // (2) 3-fold repetition
        // (3) this is xth turn, turn limit reached
    }

    /**
     * Is terminal state (when either player is in a terminal state)
     *
     * @return
     */
    fun isTerminalState(): Boolean {
        return isTerminalStateForPlayer(attackingDirection) || isTerminalStateForPlayer(attackingDirection.reflectRow())
    }

}
