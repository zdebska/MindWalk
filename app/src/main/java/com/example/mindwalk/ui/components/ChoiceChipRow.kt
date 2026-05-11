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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindwalk.ui.theme.Green100
import com.example.mindwalk.ui.theme.Green600
import com.example.mindwalk.ui.theme.Green700

@Composable
fun ChoiceChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                color = if (active) Green700 else Color(0xFF6B7280)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (active) Green100 else Color.White.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, if (active) Green600 else Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(44.dp)
    )
}
