package com.example.mindwalk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ChoiceChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(0xFF6B9E7F)
    val activeBg = Color(0xFFE8F3ED)

    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (active) activeBg else Color.White,
            labelColor = if (active) accent else Color(0xFF666666)
        ),
        border = BorderStroke(1.dp, if (active) accent else Color(0xFFE9E9E9)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(52.dp)
    )
}
