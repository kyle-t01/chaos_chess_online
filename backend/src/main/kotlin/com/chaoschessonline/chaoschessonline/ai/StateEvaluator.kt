package com.chaoschessonline.chaoschessonline.ai

import com.chaoschessonline.chaoschessonline.model.BoardState

class StateEvaluator {
    companion object {
        fun evaluate(state: BoardState): Double {
            // find current player NORTH = minimising (attacking south)
            val playerDir:Int = state.attackingDirection.row


            return 0.0


        }



    }
}