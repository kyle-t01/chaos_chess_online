package com.chaoschessonline.chaoschessonline

import com.chaoschessonline.chaoschessonline.model.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChaosChessOnlineApplication

fun main(args: Array<String>) {
	runApplication<ChaosChessOnlineApplication>(*args)
	val game:Game = Game()
	game.printGameState()

}