package com.example.mindwalk.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 [Typography] definition for the MindWalk application.
 *
 * Only [Typography.bodyLarge] is explicitly overridden here; all other text styles inherit
 * Material 3 defaults. Individual screens apply sizes and weights directly via `fontSize`
 * and `fontWeight` modifiers rather than via named typography roles, so this object mainly
 * serves as the required [MaterialTheme] typography slot wired in [MindWalkTheme].
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily    = FontFamily.Default,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.5.sp
    )
)
