package com.example.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides a high-contrast glowing border and subtle scale effect when focused
 * via Android TV D-Pad or hardware keyboard.
 */
fun Modifier.dpadFocusable(
    shape: Shape = RoundedCornerShape(16.dp),
    focusedBorderColor: Color = Color(0xFF38BDF8),
    borderWidth: Dp = 2.5.dp,
    scaleOnFocus: Float = 1.04f
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    this
        .onFocusChanged { isFocused = it.isFocused }
        .then(
            if (isFocused) {
                Modifier
                    .scale(scaleOnFocus)
                    .border(borderWidth, focusedBorderColor, shape)
            } else {
                Modifier
            }
        )
}

