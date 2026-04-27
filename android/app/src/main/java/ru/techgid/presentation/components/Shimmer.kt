package ru.techgid.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    val shimmerColors = listOf(
        TechGidTheme.extendedColors.cardBorder.copy(alpha = 0.3f),
        TechGidTheme.extendedColors.cardBorder.copy(alpha = 0.6f),
        TechGidTheme.extendedColors.cardBorder.copy(alpha = 0.3f),
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim),
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    cornerRadius: Dp = 6.dp,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush()),
    )
}

@Composable
fun SkeletonGuideCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    height = 18.dp,
                )
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth(0.6f),
                    height = 14.dp,
                )
                ShimmerBox(
                    modifier = Modifier.width(70.dp),
                    height = 20.dp,
                    cornerRadius = 6.dp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(5) {
                        ShimmerBox(
                            modifier = Modifier.size(14.dp),
                            cornerRadius = 3.dp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            ShimmerBox(
                modifier = Modifier.size(80.dp),
                cornerRadius = 12.dp,
            )
        }
    }
}

@Composable
fun SkeletonGuideDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f), height = 28.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.95f), height = 14.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f), height = 14.dp)
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f), height = 14.dp)
        Spacer(Modifier.height(4.dp))
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 240.dp,
            cornerRadius = 20.dp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(
                modifier = Modifier.weight(1f),
                height = 48.dp,
                cornerRadius = 14.dp,
            )
            ShimmerBox(
                modifier = Modifier.weight(1f),
                height = 48.dp,
                cornerRadius = 14.dp,
            )
        }
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 56.dp,
            cornerRadius = 14.dp,
        )
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 80.dp,
            cornerRadius = 14.dp,
        )
    }
}

@Composable
fun SkeletonDiagnosticResult(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f), height = 18.dp)
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 14.dp)
                }
                Spacer(Modifier.width(12.dp))
                ShimmerBox(modifier = Modifier.width(48.dp), height = 28.dp, cornerRadius = 8.dp)
            }
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(),
                height = 6.dp,
                cornerRadius = 3.dp,
            )
            Spacer(Modifier.height(4.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f), height = 14.dp)
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.85f), height = 14.dp)
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.75f), height = 14.dp)
        }
    }
}

@Composable
fun SkeletonSymptomCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.5f),
                height = 18.dp,
            )
            repeat(3) {
                Row {
                    ShimmerBox(
                        modifier = Modifier.size(20.dp),
                        cornerRadius = 4.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        height = 16.dp,
                    )
                }
            }
        }
    }
}
