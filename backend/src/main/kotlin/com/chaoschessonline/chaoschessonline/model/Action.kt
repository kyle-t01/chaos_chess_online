package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

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
        fun fromString(actionStr:String):Action? {
            // ie: "0,0 1,1"
            val posStrList:List<String> = actionStr.split(" ")
            val vList:MutableList<Vector2D> = mutableListOf()
            for (s in posStrList) {
                vList.add(Vector2D.fromStr(s))
            }
            require(vList.size == 2)
            // vList to Action
            val newFrom = vList[0]
            val newTo = vList[1]
            return Action(newFrom, newTo)
        }
    }
}
