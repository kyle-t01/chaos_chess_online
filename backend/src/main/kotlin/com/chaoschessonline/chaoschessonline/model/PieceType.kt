package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

// define uppercase as UP (team north/ team up)
enum class PieceType {
    PAWN, // P, p
    BISHOP, // B, b
    KNIGHT, // N, n
    ROOK, // R, r
    QUEEN, // Q, q
    KING, // K, k
    FOOT_SOLDIER, // Z, z (zu)
    SCHOLAR, // S, s (shi)
    ADVISOR, // X, x (xiang)
    HORSE, // M, m (ma)
    CHARIOT, // J, j (ju)
    CANNON, // C, c (pao)
    GENERAL,; // G, g

    companion object{
        fun toChar(type:PieceType, attackDirection:Vector2D):Char {
            val isNorthPlayer:Boolean = (attackDirection == Vector2D.SOUTH)
            val char = when(type) {
                PAWN -> 'P'
                BISHOP -> 'B'
                KNIGHT -> 'N'
                ROOK -> 'R'
                QUEEN -> 'Q'
                KING -> 'K'
                FOOT_SOLDIER -> 'Z'
                SCHOLAR -> 'S'
                ADVISOR -> 'X'
                HORSE -> 'M'
                CHARIOT -> 'J'
                CANNON -> 'C'
                GENERAL -> 'G'
            }
            return if (isNorthPlayer) char else char.lowercaseChar()
        }

        fun isCharEnemy(us: Char, them: Char):Boolean {
            if (us == ' ' || them == ' ') return false
            return us.isUpperCase() != them.isUpperCase()
        }
    }
}