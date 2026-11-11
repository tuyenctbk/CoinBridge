package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo600
import kotlinx.coroutines.launch

@Composable
fun CalculatorKeypad(
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val rows = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", "00", ".", "=")
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 8.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
        ) {
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp)
                ) {
                    rowKeys.forEach { key ->
                        TactileKeypadButton(
                            key = key,
                            isCompact = isCompact,
                            onClick = { onKeyClick(key) },
                            onLongClick = if (key == "⌫") { { onKeyClick("C") } } else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("key_$key")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TactileKeypadButton(
    key: String,
    isCompact: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    var isFocused by remember { mutableStateOf(false) }

    val isOperator = key == "+" || key == "-" || key == "×" || key == "÷"
    val isAction = key == "C" || key == "⌫" || key == "%"
    val isEquals = key == "="

    val backgroundColor = when {
        isEquals -> Emerald500
        isOperator -> Indigo600
        isAction -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isEquals -> Color.White
        isOperator -> Color.White
        isAction -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(if (isCompact) 42.dp else 54.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                val s = if (isFocused) scale.value * 1.05f else scale.value
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFocused) backgroundColor.copy(alpha = 0.9f) else backgroundColor)
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = if (isFocused) Color(0xFF38BDF8) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = {
                    // Tactile Haptic Feedback
                    if (isEquals) {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    } else {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                    coroutineScope.launch {
                        scale.animateTo(0.92f, animationSpec = spring())
                        scale.animateTo(1.0f, animationSpec = spring())
                    }
                    onClick()
                },
                onLongClick = onLongClick?.let { action ->
                    {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        coroutineScope.launch {
                            scale.animateTo(0.88f, animationSpec = spring())
                            scale.animateTo(1.0f, animationSpec = spring())
                        }
                        action()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            fontSize = if (isCompact) (if (isOperator || isAction) 17.sp else 19.sp) else (if (isOperator || isAction) 20.sp else 22.sp),
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
