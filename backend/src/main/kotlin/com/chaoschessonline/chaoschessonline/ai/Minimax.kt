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

            // first select a random piece within this list
            val src:Int = Random.nextInt(0, ownPieces.size)

            // generate valid destination for this piece
            val destList= ValidActionGenerator.findPossibleActionsForIndex(src, state)

            // choose one random destination
            val dest = Random.nextInt(0, destList.size)

            // apply action
            val newState = state.applyAction(src, dest)
            return newState
        }
    }
}