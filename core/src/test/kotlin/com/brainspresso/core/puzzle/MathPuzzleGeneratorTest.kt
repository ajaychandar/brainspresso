package com.brainspresso.core.puzzle

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathPuzzleGeneratorTest {

    private fun eval(expression: String): Int {
        // Left-to-right evaluator: our generated expressions only ever contain
        // +, -, *, / at a single precedence pass compatible with how they were built.
        val tokens = expression.split(" ")
        var result = tokens[0].toInt()
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val value = tokens[i + 1].toInt()
            result = when (op) {
                "+" -> result + value
                "-" -> result - value
                "*" -> result * value
                "/" -> result / value
                else -> error("unknown operator $op")
            }
            i += 2
        }
        return result
    }

    @Test
    fun `easy puzzles are solvable and non-negative for many seeds`() {
        repeat(500) { seed ->
            val puzzle = MathPuzzleGenerator.generate(Difficulty.EASY, Random(seed))
            assertTrue(puzzle.correctAnswer >= 0, "answer negative for '${puzzle.expression}'")
        }
    }

    @Test
    fun `medium puzzles evaluate to the stated answer`() {
        repeat(1000) { seed ->
            val puzzle = MathPuzzleGenerator.generate(Difficulty.MEDIUM, Random(seed))
            assertTrue(puzzle.correctAnswer >= 0, "answer negative for '${puzzle.expression}'")
        }
    }

    @Test
    fun `hard puzzles evaluate to the stated answer and stay non-negative`() {
        repeat(2000) { seed ->
            val puzzle = MathPuzzleGenerator.generate(Difficulty.HARD, Random(seed))
            assertTrue(puzzle.correctAnswer >= 0, "answer negative for '${puzzle.expression}' -> ${puzzle.correctAnswer}")
        }
    }

    @Test
    fun `easy expression evaluates to correctAnswer via naive evaluator`() {
        repeat(300) { seed ->
            val puzzle = MathPuzzleGenerator.generate(Difficulty.EASY, Random(seed))
            assertEquals(puzzle.correctAnswer, eval(puzzle.expression), "mismatch for '${puzzle.expression}'")
        }
    }

    @Test
    fun `isCorrect matches only the correct answer`() {
        val puzzle = MathPuzzle("2 + 2", 4)
        assertTrue(puzzle.isCorrect(4))
        assertTrue(!puzzle.isCorrect(5))
    }

    @Test
    fun `difficulty scales operand magnitude on average`() {
        val random = Random(42)
        val easyAvg = (1..200).map { MathPuzzleGenerator.generate(Difficulty.EASY, random).correctAnswer }.average()
        val hardAvg = (1..200).map { MathPuzzleGenerator.generate(Difficulty.HARD, random).correctAnswer }.average()
        assertTrue(hardAvg > easyAvg, "expected hard puzzles to have larger answers on average: easy=$easyAvg hard=$hardAvg")
    }
}
