package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 650,
                easing = FastOutSlowInEasing
            )
        )
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(1100)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    ),
                    radius = 1200f
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .padding(24.dp)
        ) {
            // Glowing Branded CoinBridge Vector Graphic Emblem
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(glowPulse)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = 0.25f),
                                    Color(0xFF6366F1).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Custom Canvas Drawing the CoinBridge Arch & Coins
                Canvas(modifier = Modifier.size(100.dp)) {
                    val w = size.width
                    val h = size.height

                    // Bridge Arc
                    val bridgePath = Path().apply {
                        moveTo(w * 0.25f, h * 0.65f)
                        cubicTo(
                            w * 0.35f, h * 0.28f,
                            w * 0.65f, h * 0.28f,
                            w * 0.75f, h * 0.65f
                        )
                    }
                    drawPath(
                        path = bridgePath,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Exchange Arrow Above Bridge
                    val arrowPath = Path().apply {
                        moveTo(w * 0.40f, h * 0.36f)
                        lineTo(w * 0.60f, h * 0.36f)
                        moveTo(w * 0.54f, h * 0.30f)
                        lineTo(w * 0.60f, h * 0.36f)
                        lineTo(w * 0.54f, h * 0.42f)
                    }
                    drawPath(
                        path = arrowPath,
                        color = Color(0xFF818CF8),
                        style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Left Fiat Coin (Emerald)
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 16.dp.toPx(),
                        center = Offset(w * 0.28f, h * 0.55f)
                    )

                    // Right Crypto Coin (Amber)
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 16.dp.toPx(),
                        center = Offset(w * 0.72f, h * 0.55f)
                    )
                }

                // Currency labels over the drawn coins
                Row(
                    modifier = Modifier.width(100.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₿",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Security badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.privacy_biometric_lock),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1)
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            CircularProgressIndicator(
                color = Color(0xFF10B981),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
