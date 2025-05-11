package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.ai.StateEvaluator
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
        fun testBoardState(board: Board, atkDir: Vector2D) = BoardState(null, board, 0, atkDir)

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
     * @return A list of Vector2D representing positions
     */
    private fun findAttackingPieces(atkDir: Vector2D): List<Vector2D> = board.findPositionsOfAtkDir(atkDir)

    /**
     * Find current attacking pieces
     *
     * Use the attacking direction of current moving player
     *
     * @return
     */
    fun findCurrentAttackingPieces(): List<Vector2D> = findAttackingPieces(attackingDirection)

    /**
     * Find current enemy pieces
     *
     * @return
     */
    fun findCurrentEnemyPieces(): List<Vector2D> = findAttackingPieces(attackingDirection.reflectRow())


    /**
     * Is terminal state (when either player is in a terminal state)
     *
     * @return
     */
    fun isTerminalState(): Boolean {
        return hasPlayerLost() || hasEnemyLost()
    }


    fun hasPlayerLost(): Boolean {
        // no more valid moves
        // assume that the enemy hasn't lost first
        // are we able to make any valid moves? (includes moves that allows current leader ot be captured)
        val nextStates = generateNextStates()
        if (nextStates.isEmpty()) return true

        // make moves that don't keep our leader in threat
        val threatAwareNextStates = filterThreatAwareSubset(nextStates)
        if (threatAwareNextStates.isEmpty()) return true

        return false
    }

    fun hasEnemyLost(): Boolean  {
        val enemyState = this.flipPlayer()
        return enemyState.hasPlayerLost()
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
        // sanity check: if we can win immediately, filterThreatAware should not reject that state

        val threatAwareStates = filterThreatAwareSubset(nextStates)
        // if regardless whether empty, return
        return threatAwareStates
    }


    fun isAbleToCaptureEnemyLeader(): Boolean {
        // are we able to immediately capture enemy leader?

        return true
    }

    fun isLeaderUnderThreat(): Boolean {
        // implemented as: skip player's turn, will enemy cause all leaders to die?
        // find where enemy can threaten
        val threats: List<Action> = ValidActionGenerator.findAllEnemyThreats(this)
        if (threats.isEmpty()) return false

        // find the positions of original leaders
        val ourPieces = findCurrentAttackingPieces()
        val ourLeader = findLeaderInPosList(ourPieces)
        require(ourLeader != null) {"ERROR: NO LEADER?! (return false)"}
        val leaderUnderThreat = threats.any{it.to == ourLeader}

        return leaderUnderThreat
    }

    fun canCaptureEnemyLeader(): Boolean {
        // idea: can we immediately capture the enemy leader? very similar to isLeaderUnderThreat
        // canCaptureEnemyLeader() == isLeaderUnderThreat() from enemy's perspective
        val enemyPerspective = this.flipPlayer()
        return enemyPerspective.isLeaderUnderThreat()
    }

    fun flipPlayer(): BoardState {
        return BoardState(parent, board, turnNumber,attackingDirection.reflectRow())
    }
    // find leader
    fun findLeaderInPosList(posList: List<Vector2D>): Vector2D? {
        return board.findLeaderFromPositions(posList)
    }

    fun prettyPrintProgress() {
        val end = this
        var curr:BoardState? = end
        while(curr != null) {
            println("###")
            println("turnNumber: $turnNumber, atkDir = $attackingDirection")
            println("evaluation: ${StateEvaluator.findTacticalScore(this)}")
            board.prettyPrint()
            println("---")
            curr = curr.parent
        }

    }

}
