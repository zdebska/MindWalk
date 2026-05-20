package com.example.mindwalk.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Titled card container used to group related configuration options on planning screens.
 *
 * Renders a semi-transparent white [Surface] with a bold section title followed by
 * the caller-supplied [content] slot. Used by
 * [com.example.mindwalk.ui.screens.PlanYourWalkScreen] for the "Start Location",
 * "Walk Goals", and "Mindfulness Mode" sections.
 *
 * @param title    Text shown as the section heading.
 * @param modifier Layout modifier applied to the card surface.
 * @param content  Composable slot rendered below the title, scoped to a [ColumnScope].
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = Color.White.copy(alpha = 0.7f)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = Color(0xFF1F2937)
            )
            content()
        }
    }
}
