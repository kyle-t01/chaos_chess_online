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


        /**
         * Is ally
         *
         * @param us
         * @param them
         * @return
         */
        fun isAlly(us: Char, them: Char):Boolean {
            require(us != ' ') {"us char must not be empty!"}
            if (them == ' ') return false
            return us.isUpperCase() == them.isUpperCase()
        }

        /**
         * Is enemy
         *
         * @param us
         * @param them
         * @return
         */
        fun isEnemy(us: Char, them: Char):Boolean {
            require(us != ' '){"us char must not be empty!"}
            if (them == ' ') return false
            return us.isUpperCase() != them.isUpperCase()
        }

        /**
         * Find attack direction of a char
         *
         * assume that passed in an non-empty char
         *
         * @param c
         * @return
         */
        fun findAttackDirection(c:Char):Vector2D {
            require(c != ' ') {"Can't find an attack direction of empty char!!!"}
            val isNorthPlayer:Boolean = c.isUpperCase()
            return if (isNorthPlayer) Vector2D.SOUTH else Vector2D.NORTH
        }

        /**
         * Is piece of attacker, given piece and attackingDirection
         *
         * @param c
         * @param attackDirection
         * @return
         */
        fun isPieceOfAttacker(c:Char, attackDirection: Vector2D):Boolean {
            val isNorthPlayer = c.isUpperCase()
            val pieceAttackDirection = findAttackDirection(c)
            return attackDirection == pieceAttackDirection
        }

    }
}