package com.brainspresso.core.puzzle

import kotlin.random.Random

/**
 * Generates arithmetic puzzles that always evaluate to a non-negative integer.
 * Difficulty scales the operand range, operator count, and whether multiplication/division appear.
 */
object MathPuzzleGenerator {

    fun generate(difficulty: Difficulty, random: Random = Random.Default): MathPuzzle {
        return when (difficulty) {
            Difficulty.EASY -> generateEasy(random)
            Difficulty.MEDIUM -> generateMedium(random)
            Difficulty.HARD -> generateHard(random)
        }
    }

    private fun generateEasy(random: Random): MathPuzzle {
        val a = random.nextInt(1, 21)
        val b = random.nextInt(1, 21)
        return if (random.nextBoolean()) {
            MathPuzzle("$a + $b", a + b)
        } else {
            val (hi, lo) = if (a >= b) a to b else b to a
            MathPuzzle("$hi - $lo", hi - lo)
        }
    }

    private fun generateMedium(random: Random): MathPuzzle {
        val a = random.nextInt(2, 13)
        val b = random.nextInt(2, 13)
        return when (random.nextInt(3)) {
            0 -> {
                val c = random.nextInt(1, 26)
                MathPuzzle("$a * $b + $c", a * b + c)
            }
            1 -> {
                val product = a * b
                val c = random.nextInt(0, product + 1)
                MathPuzzle("$a * $b - $c", product - c)
            }
            else -> {
                val c = random.nextInt(1, 26)
                MathPuzzle("$a + $b + $c", a + b + c)
            }
        }
    }

    private fun generateHard(random: Random): MathPuzzle {
        val a = random.nextInt(3, 21)
        val b = random.nextInt(2, 13)
        val d = random.nextInt(1, 51)
        return when (random.nextInt(4)) {
            0 -> {
                val product = a * b
                val c = random.nextInt(0, product + 1)
                MathPuzzle("$a * $b - $c", product - c)
            }
            1 -> {
                // Ensure clean integer division: build (quotient * divisor) / divisor
                val divisor = random.nextInt(2, 10)
                val quotient = random.nextInt(2, 15)
                val dividend = divisor * quotient
                MathPuzzle("$dividend / $divisor + $d", quotient + d)
            }
            2 -> {
                val e = (d % 9) + 1
                val c = random.nextInt(2, 13)
                MathPuzzle("$a * $b + $c * $e", a * b + c * e)
            }
            else -> {
                val c = random.nextInt(2, 13)
                val sum = a + b + c
                val subtrahend = d % (sum + 1)
                MathPuzzle("$a + $b + $c - $subtrahend", sum - subtrahend)
            }
        }
    }
}
