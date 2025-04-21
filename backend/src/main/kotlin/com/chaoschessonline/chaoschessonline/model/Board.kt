package com.chaoschessonline.chaoschessonline.model

import com.chaoschessonline.chaoschessonline.util.Vector2D

/**
 * Board
 *
 * @property playerPieceList
 * @property enemyPieceList
 * @constructor Create empty Board
 */
data class Board(
    val playerPieceList: Map<Vector2D, PieceType> = mapOf(),
    val enemyPieceList: Map<Vector2D, PieceType> = mapOf()
)
{
    companion object {
        fun defaultBoard() = Board(XIANGQI_PIECES_BOTTOM_HALF, CHESS_PIECES_TOP_HALF)

        val XIANGQI_PIECES_BOTTOM_HALF: Map<Vector2D, PieceType> = mapOf(
            Vector2D(1,0) to PieceType.HORSE,
            Vector2D(4,0) to PieceType.HORSE,
            Vector2D(2,0) to PieceType.SCHOLAR,
            Vector2D(3,0) to PieceType.GENERAL,
            Vector2D(1,1) to PieceType.CANNON,
            Vector2D(4,1) to PieceType.CANNON,
            Vector2D(0,2) to PieceType.FOOT_SOLDIER,
            Vector2D(2,2) to PieceType.FOOT_SOLDIER,
            Vector2D(3,2) to PieceType.FOOT_SOLDIER,
            Vector2D(5,2) to PieceType.FOOT_SOLDIER,
        )

        val CHESS_PIECES_TOP_HALF: Map<Vector2D, PieceType> = mapOf(
            Vector2D(0,5) to PieceType.ROOK,
            Vector2D(1,5) to PieceType.BISHOP,
            Vector2D(2,5) to PieceType.SCHOLAR,
            Vector2D(3,5) to PieceType.QUEEN,
            Vector2D(4,5) to PieceType.KING,
            Vector2D(5,5) to PieceType.ROOK,
            Vector2D(0,4) to PieceType.PAWN,
            Vector2D(1,4) to PieceType.PAWN,
            Vector2D(2,4) to PieceType.PAWN,
            Vector2D(3,4) to PieceType.PAWN,
            Vector2D(4,4) to PieceType.PAWN,
            Vector2D(5,4) to PieceType.PAWN,
        )

        val DIMENSION:Vector2D = Vector2D(6,6)
    }


}
