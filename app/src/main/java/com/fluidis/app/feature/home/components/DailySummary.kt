package com.fluidis.app.feature.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.theme.GoalGold
import com.fluidis.app.core.theme.GoalGreen
import com.fluidis.app.core.theme.OverUpperAmber
import com.fluidis.app.core.ui.AnimationConstants

@Composable
fun DailySummary(
    totalMl: Int,
    goalMl: Int,
    goalUpperMl: Int?,
    progressFraction: Float,
    goalReached: Boolean,
    overUpperBound: Boolean,
    remainingMl: Int,
    overGoalMl: Int,
    overUpperMl: Int,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = AnimationConstants.PROGRESS_DURATION_MS),
        label = "water_fill",
    )

    val hasRange = goalUpperMl != null && goalUpperMl > goalMl

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WaterDropIndicator(
                progress = animatedProgress,
                goalReached = goalReached,
                overUpperBound = overUpperBound,
                modifier = Modifier.size(120.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (hasRange) "$totalMl / $goalMl–$goalUpperMl ml" else "$totalMl / $goalMl ml",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                when {
                    overUpperBound -> {
                        Text(
                            text = "Over upper limit! +$overUpperMl ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OverUpperAmber,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    goalReached -> {
                        Text(
                            text = if (overGoalMl > 0) "Goal reached! +$overGoalMl ml" else "Goal reached!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoalGreen,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    else -> {
                        Text(
                            text = "$remainingMl ml remaining",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${(progressFraction * 100).toInt().coerceAtMost(999)}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WaterDropIndicator(
    progress: Float,
    goalReached: Boolean,
    overUpperBound: Boolean,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val dropPath = createWaterDropPath(size.width, size.height)

        // Background (empty drop)
        clipPath(dropPath) {
            drawRect(color = surfaceVariant)
        }

        // Filled portion
        clipPath(dropPath) {
            val fillTop = size.height * (1f - progress)

            val fillColor = when {
                overUpperBound -> Brush.verticalGradient(
                    colors = listOf(OverUpperAmber, Color(0xFFD84315)),
                    startY = fillTop,
                    endY = size.height,
                )
                goalReached -> Brush.verticalGradient(
                    colors = listOf(GoalGold, GoalGreen),
                    startY = fillTop,
                    endY = size.height,
                )
                else -> Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.7f),
                        primaryColor,
                    ),
                    startY = fillTop,
                    endY = size.height,
                )
            }

            drawRect(
                brush = fillColor,
                topLeft = Offset(0f, fillTop),
                size = androidx.compose.ui.geometry.Size(size.width, size.height - fillTop),
            )
        }

        // Subtle outline
        val outlineColor = when {
            overUpperBound -> OverUpperAmber.copy(alpha = 0.5f)
            goalReached -> GoalGreen.copy(alpha = 0.5f)
            else -> primaryColor.copy(alpha = 0.3f)
        }
        drawPath(
            path = dropPath,
            color = outlineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
        )
    }
}

private fun DrawScope.createWaterDropPath(width: Float, height: Float): Path {
    return Path().apply {
        val cx = width / 2f
        val tipY = height * 0.05f
        val bottomY = height * 0.95f
        val bulgeRadius = width * 0.42f

        moveTo(cx, tipY)

        // Left curve from tip to bottom bulge
        cubicTo(
            cx - bulgeRadius * 0.3f, height * 0.25f,
            cx - bulgeRadius, height * 0.5f,
            cx - bulgeRadius, height * 0.65f,
        )

        // Left bottom curve
        cubicTo(
            cx - bulgeRadius, height * 0.82f,
            cx - bulgeRadius * 0.6f, bottomY,
            cx, bottomY,
        )

        // Right bottom curve
        cubicTo(
            cx + bulgeRadius * 0.6f, bottomY,
            cx + bulgeRadius, height * 0.82f,
            cx + bulgeRadius, height * 0.65f,
        )

        // Right curve back to tip
        cubicTo(
            cx + bulgeRadius, height * 0.5f,
            cx + bulgeRadius * 0.3f, height * 0.25f,
            cx, tipY,
        )

        close()
    }
}
