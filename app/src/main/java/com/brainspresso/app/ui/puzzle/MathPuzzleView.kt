package com.brainspresso.app.ui.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.MathPuzzle

private const val MAX_ANSWER_DIGITS = 8

@Composable
fun MathPuzzleView(
    puzzle: MathPuzzle,
    onSolved: () -> Unit,
    onFailed: () -> Unit,
) {
    var answer by remember(puzzle) { mutableStateOf("") }

    fun submit() {
        val value = answer.toIntOrNull()
        if (value != null && puzzle.isCorrect(value)) onSolved() else onFailed()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.Filled.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Cognitive ignition",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        Text(
            text = puzzle.expression.replace("*", "×").replace("/", "÷") + " = ?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 14.dp),
        )
        Box(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(width = 2.dp, height = 28.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val digitRows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            digitRows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { digit ->
                        KeyButton(
                            modifier = Modifier.weight(1f),
                            background = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = { if (answer.length < MAX_ANSWER_DIGITS) answer += digit },
                        ) {
                            Text(digit, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                KeyButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.surfaceContainerHighest,
                    onClick = { answer = answer.dropLast(1) },
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
                KeyButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.surfaceContainerHigh,
                    onClick = { if (answer.length < MAX_ANSWER_DIGITS) answer += "0" },
                ) {
                    Text("0", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                KeyButton(
                    modifier = Modifier.weight(1f),
                    background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (answer.isNotBlank()) 1f else 0.4f),
                    enabled = answer.isNotBlank(),
                    onClick = ::submit,
                ) {
                    Text("OK", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier,
    background: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
