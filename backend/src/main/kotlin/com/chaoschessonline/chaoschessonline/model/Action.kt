package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D
import java.awt.Dimension

/**
 * Action
 *
 * represents movement between two positions
 *
 * @property from
 * @property to
 * @constructor Create Action
 */
data class Action(
    val from:Vector2D,
    val to:Vector2D,
) {
    companion object {
        /**
         * From string
         *
         * @param actionStr
         * @return
         */
        fun fromString(actionStr:String):Action {
            // ie: "0,0 1,1"
            val posStrList:List<String> = actionStr.split(" ")
            val vList:MutableList<Vector2D> = mutableListOf()
            for (s in posStrList) {
                vList.add(Vector2D.fromStr(s))
            }
            require(vList.size == 2) {"Error: Action could not be made from STRING"}
            // vList to Action
            val newFrom = vList[0]
            val newTo = vList[1]
            return Action(newFrom, newTo)
        }

        /**
         * From indices
         *
         * Supply from, to, and board dimensions
         *
         * @param from
         * @param to
         * @param dimension
         * @return an Action
         */
        fun fromIndices(from:Int, to:Int, dimension: Vector2D): Action {
            val src: Vector2D = Vector2D.fromIndex(from, dimension)
            val dest: Vector2D = Vector2D.fromIndex(to, dimension)
            return Action(src, dest)
        }
    }
}
