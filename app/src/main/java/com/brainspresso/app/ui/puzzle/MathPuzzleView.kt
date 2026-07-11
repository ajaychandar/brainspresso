package com.brainspresso.app.ui.puzzle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.brainspresso.core.puzzle.MathPuzzle

@Composable
fun MathPuzzleView(
    puzzle: MathPuzzle,
    onSolved: () -> Unit,
    onFailed: () -> Unit,
) {
    var answer by remember(puzzle) { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = puzzle.expression + " = ?",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = answer,
            onValueChange = { input -> answer = input.filter { it.isDigit() || it == '-' }.take(8) },
            label = { Text("Your answer") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
            modifier = Modifier.width(200.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                val value = answer.toIntOrNull()
                if (value != null && puzzle.isCorrect(value)) onSolved() else onFailed()
            },
            enabled = answer.isNotBlank(),
        ) {
            Text("Submit")
        }
    }
}
