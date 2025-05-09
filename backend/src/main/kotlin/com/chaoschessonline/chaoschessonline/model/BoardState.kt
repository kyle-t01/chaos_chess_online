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
    // possibly keep track where this state (if non-terminal) leads to a guaranteed win or loss
) {
    var eval: Double = 0.0

    companion object {
        fun defaultBoardState() = BoardState(null, Board.defaultBoard(), 0, Vector2D.NORTH)
        fun northStartBoardState() = BoardState(null, Board.defaultBoard(), 0, Vector2D.SOUTH)
        fun testBoardState(board: Board, atkDir: Vector2D) = BoardState(null, board, 0, atkDir)
        fun debugBoardState() = BoardState(null,  Board.fromString(" , m,  , s, m,  ,  ,  , g, c,  ,  , z, P, z, z,  , z,  ,  , P,  ,  ,  , P,  ,  , P, P, P, R, c, K, Q, B, R"), 0, Vector2D.NORTH)

        /**
         * Filter threat aware subset
         *
         * @param nextStates
         * @return threatAware BoardStates (may be empty)
         */
        fun filterThreatAwareSubset(nextStates: List<BoardState>): List<BoardState> {
            // reject moves that will cause it to lose, but don't reject moves that will cause it to win
            val threatAware = nextStates.filter { !it.canCaptureEnemyLeader()}
            return threatAware
        }

    }

    /**
     * To hash str
     *
     * @return
     */
    fun toHashStr(): String {
        return board.board.joinToString("") + attackingDirection
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
        if (isActionOutsideTurn(action)) {
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
    fun applyAction(from: Int, to: Int): BoardState {
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
    fun findAttackingPieces(atkDir: Vector2D): List<Int> = board.findAttackerIndices(atkDir)

    /**
     * Find current attacking pieces
     *
     * Use the attacking direction of current moving player
     *
     * @return
     */
    fun findCurrentAttackingPieces(): List<Int> = findAttackingPieces(attackingDirection)

    /**
     * Find current enemy pieces
     *
     * @return
     */
    fun findCurrentEnemyPieces(): List<Int> = findAttackingPieces(attackingDirection.reflectRow())


    /**
     * Is terminal state (when either player is in a terminal state)
     *
     * @return
     */
    fun isTerminalState(): Boolean {
        val enemyState = this.flipPlayer()
        return isTerminalStateForCurrentPlayer() || enemyState.isTerminalStateForCurrentPlayer()
    }

    /**
     * Is terminal state for current player
     *
     * @return
     */
    fun isTerminalStateForCurrentPlayer(): Boolean {
        // no valid moves left (nextStates)
        val nextStates = generateNextStates()
        if (nextStates.isEmpty()) return true
        // no valid threat-aware moves (won't move in way that allows leader to be captured)
        val threatAwareNextStates = BoardState.filterThreatAwareSubset(nextStates)
        if (threatAwareNextStates.isEmpty()) return true
        return false
    }


    /**
     * Generate next states of a BoardState
     *
     *
     * @return List of BoardState
     */
    fun generateNextStates(): List<BoardState> {
        // generate all children
        val actions = ValidActionGenerator.findAllValidActions(this)
        val nextStates = actions.map { applyAction(it) }
        return nextStates
    }

    /**
     * Generate threat aware next states (rejecting moves that keep us under threat)
     *
     * @return
     */
    fun generateThreatAwareNextStates(): List<BoardState> {
        // generate all children
        val nextStates = generateNextStates()
        val threatAwareStates = filterThreatAwareSubset(nextStates)
        // if regardless whether empty, return
        return threatAwareStates
    }


    fun isLeaderUnderThreat(): Boolean {

        // implemented as: skip player's turn, will enemy cause all leaders to die?
        // find where enemy can threaten
        val threats: List<Action> = ValidActionGenerator.findAllEnemyThreats(this)
        if (threats.isEmpty()) return false

        // find the positions of original leaders
        val ourLeaders = findLeaderPositions()
        if (ourLeaders.isEmpty()) return false

        // when threats (enemy actions) are applied, do they threaten all (for now, 1 per side) our leaders?
        require(ourLeaders.size <= 1) {"LOGIC FOR MULTIPLE LEADERS NOT IMPLEMENTED YET"}
        val onlyLeader = ourLeaders[0]
        val leaderUnderThreat = threats.any{it.to == onlyLeader}

        return leaderUnderThreat
    }

    fun canCaptureEnemyLeader(): Boolean {
        // idea: can we immediately capture the enemy leader? very similar to isLeaderUnderThreat
        // canCaptureEnemyLeader() == isLeaderUnderThreat() from enemy's perspective
        val enemyPerspective = this.flipPlayer()
        return enemyPerspective.isLeaderUnderThreat()
    }

    fun flipPlayer(): BoardState {
        return BoardState(parent, board, turnNumber,attackingDirection.reflectRow() )
    }

    /**
     * Find leader positions
     *
     * @return
     */
    fun findLeaderPositions(): List<Vector2D> {
        val pieces = findCurrentAttackingPieces()
        val ourLeaders =  pieces.filter{board.isLeaderInIndex(it)}.map{Vector2D.fromIndex(it,Board.DEFAULT_DIMENSION)}
        return ourLeaders
    }

    /**
     * Find enemy leader positions
     *
     * @return
     */
    fun findEnemyLeaderPositions(): List<Vector2D> {
        return this.flipPlayer().findLeaderPositions()
    }

}
