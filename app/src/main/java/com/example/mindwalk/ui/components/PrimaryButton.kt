package com.example.mindwalk.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindwalk.ui.theme.Green600

/**
 * Full-width primary action button used as the main call-to-action across planning screens.
 *
 * Renders a 56 dp tall [Button] with [Green600] background, white bold text, and rounded
 * corners. Used by [com.example.mindwalk.ui.screens.PlanYourWalkScreen] for "Generate Route".
 *
 * @param text    Label displayed inside the button.
 * @param onClick Invoked when the button is tapped.
 */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = Green600)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}
