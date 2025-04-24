package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.util.Vector2D
import com.chaoschessonline.chaoschessonline.model.BoardState
import com.chaoschessonline.chaoschessonline.model.Action
import com.chaoschessonline.chaoschessonline.model.ValidActionGenerator

import kotlin.random.Random

class Minimax {

    companion object {
        fun minimax(state:BoardState, depth:Int, atkDir:Vector2D) {
            ;
        }

        fun makeRandomAction(state:BoardState): BoardState {
            // make a random action, depending on current boardstate
            val ownPieces = state.findCurrentAttackingPieces()
            if (ownPieces.size == 0) return state;

            println("###makeRandomAction()###")
            println("##Attacking Pieces:")
            for (p in ownPieces) {
                println("$p: ${state.board.getPieceChar(p)}")
            }
            println("####")

            // for all pieces, find the first piece that has a valid destination
            var src:Int = -1
            val validDest:MutableList<Int> = mutableListOf()
            for (p in ownPieces) {
                val destList = ValidActionGenerator.findPossibleActionsForIndex(p, state)
                if (destList.size > 0) {
                    validDest.addAll(destList)
                    src = p
                    break
                }
            }
            if (validDest.size == 0) return state;
            println("# picked SRC piece $src: ${state.board.getPieceChar(src)}")


            // select a destination found
            println("validDests = $validDest")
            val dest:Int = validDest[Random.nextInt(0, validDest.size)]
            println("# picked DEST piece $dest: ${state.board.getPieceChar(dest)}")

            val newState = state.applyAction(src, dest)

            return newState
        }

        fun makeGreedyAction(state: BoardState): BoardState {
            // (1) find all possible actions
            val pieces = state.findCurrentAttackingPieces()
            val actions = ValidActionGenerator.findActionsOfList(pieces, state)
            // (2) get a list of next possible states
            val nextStates:MutableList<BoardState> = mutableListOf()
            for (a in actions) {
                // apply each action to get a board state
                val next = state.applyAction(a)
                nextStates.add(next)
            }
            // (3) evaluation of states, and find best state
            println("### makeGreedyAction() ###")
            // eval from perspective of max or min player
            val playerDir:Int = state.attackingDirection.row
            // -1 means attack downwards, north player is min player
            val maxPlayer: Boolean = (playerDir != -1)
            val worstEvalOfThisPlayer = if (maxPlayer) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
            var evalWanted = worstEvalOfThisPlayer
            for (s in nextStates) {
                val eval = StateEvaluator.evaluate(s)
                // TODO: should set evaluation within the boardstate object
                s.eval = eval
                println("${s.board} has eval of $eval")
                if (maxPlayer && (eval > evalWanted)) {
                    evalWanted = eval

                    continue;
                }
                if (!maxPlayer && (eval < evalWanted)) {
                    evalWanted = eval
                    continue;
                }
            }

            println("The evalWanted is $evalWanted")
            // now do a linear search
            for (s in nextStates) {
                if (s.eval == evalWanted) {
                    println("### -end- ###")
                    return s
                }
            }

            require(false) {"ERROR, should not have reached here!"}
            // (4) return that best state
            return state
        }
    }
}