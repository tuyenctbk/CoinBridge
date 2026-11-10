package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricalRatePoint
import com.example.ui.calculator.ExpressionEvaluator.toCleanString

@Composable
fun RateTrendChart(
    points: List<HistoricalRatePoint>,
    fromCode: String,
    toCode: String,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No historical data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val rates = points.map { it.rate }
    val minRate = rates.minOrNull() ?: 0.0
    val maxRate = rates.maxOrNull() ?: 1.0
    val avgRate = rates.average()
    val firstRate = rates.first()
    val lastRate = rates.last()
    val changePercent = if (firstRate > 0) ((lastRate - firstRate) / firstRate) * 100.0 else 0.0
    val isPositive = changePercent >= 0

    val lineColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
    val gradientBottom = lineColor.copy(alpha = 0.0f)
    val gradientTop = lineColor.copy(alpha = 0.28f)

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rate_trend_chart"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Stats Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$fromCode ➔ $toCode Historical Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val activePoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.last()
                    Text(
                        text = "1 $fromCode = ${activePoint.rate.toCleanString()} $toCode",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (selectedPointIndex != null) "Date: ${activePoint.date} (tap to inspect)" else "Latest: ${activePoint.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Change % Pill
                Box(
                    modifier = Modifier
                        .background(
                            color = lineColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", changePercent)}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = lineColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val width = size.width.toFloat()
                                val step = width / (points.size - 1).coerceAtLeast(1)
                                val index = (offset.x / step).toInt().coerceIn(0, points.size - 1)
                                selectedPointIndex = index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val range = (maxRate - minRate).let { if (it == 0.0) 1.0 else it }

                    // Draw subtle horizontal grid guidelines
                    val gridColor = Color.Gray.copy(alpha = 0.15f)
                    drawLine(gridColor, Offset(0f, height * 0.2f), Offset(width, height * 0.2f), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, height * 0.8f), Offset(width, height * 0.8f), strokeWidth = 1f)

                    if (points.size < 2) {
                        // Single point draw line across
                        val y = height * 0.5f
                        drawLine(lineColor, Offset(0f, y), Offset(width, y), strokeWidth = 3.dp.toPx())
                        return@Canvas
                    }

                    val path = Path()
                    val fillPath = Path()

                    points.forEachIndexed { i, point ->
                        val x = i * (width / (points.size - 1))
                        val normalizedY = ((point.rate - minRate) / range).toFloat()
                        val y = height - (normalizedY * (height * 0.75f)) - (height * 0.12f)

                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (i == points.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Draw Gradient Area Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientTop, gradientBottom),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw Trend Line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw selected highlight dot & vertical guide line
                    selectedPointIndex?.let { index ->
                        val x = index * (width / (points.size - 1))
                        val point = points[index]
                        val normalizedY = ((point.rate - minRate) / range).toFloat()
                        val y = height - (normalizedY * (height * 0.75f)) - (height * 0.12f)

                        drawLine(
                            color = lineColor.copy(alpha = 0.4f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // High / Low / Avg Stats Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Low: ${minRate.toCleanString()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Avg: ${avgRate.toCleanString()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("High: ${maxRate.toCleanString()}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
