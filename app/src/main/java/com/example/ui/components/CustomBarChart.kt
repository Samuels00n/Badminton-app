package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.NaturalCardBorder
import com.example.ui.theme.OliveAccent

data class BarChartItem(
    val label: String,
    val value: Float,
    val secondaryValue: String? = null
)

@Composable
fun CustomBarChart(
    items: List<BarChartItem>,
    title: String,
    unit: String = "%",
    modifier: Modifier = Modifier,
    barColor: Color = ForestGreenPrimary
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, NaturalCardBorder, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Zatím nejsou k dispozici žádná data pro graf",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    val maxValue = items.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 100f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, NaturalCardBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Canvas for Bar Chart Drawing
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val numBars = items.size
            val barSpacing = 20.dp.toPx()
            val availableWidth = canvasWidth - (barSpacing * (numBars + 1))
            val barWidth = (availableWidth / numBars).coerceAtLeast(16.dp.toPx())

            items.forEachIndexed { index, item ->
                val barX = barSpacing + index * (barWidth + barSpacing)
                val barHeight = (item.value / maxValue) * (canvasHeight * 0.8f)
                val barY = canvasHeight - barHeight

                // Background track
                drawRoundRect(
                    color = barColor.copy(alpha = 0.12f),
                    topLeft = Offset(barX, 0f),
                    size = Size(barWidth, canvasHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                // Filled bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // X-Axis Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${item.value.toInt()}$unit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
