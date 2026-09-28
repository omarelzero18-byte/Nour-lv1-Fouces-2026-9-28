package com.example.bennu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun BennuCharacter(
    stage: BennuStage,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    isCelebrating: Boolean = false,
    isDimmed: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bennu_idle")

    // Breathing scale oscillation - slower and calmer when dimmed
    val breathScale by infiniteTransition.animateFloat(
        initialValue = if (isDimmed) 0.98f else 0.96f,
        targetValue = if (isDimmed) 1.02f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isDimmed) 3200 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Gentle vertical bob
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = if (isDimmed) -2f else -5f,
        targetValue = if (isDimmed) 2f else 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isDimmed) 2600 else 1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Glowing halo pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isDimmed) 0.08f else 0.25f,
        targetValue = if (isDimmed) 0.18f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isDimmed) 2400 else 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Wing flap angle for celebration or idle
    val celebrationWingAngle by animateFloatAsState(
        targetValue = if (isCelebrating) 35f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "wing_celebrate"
    )

    val idleWingFlap by infiniteTransition.animateFloat(
        initialValue = if (isDimmed) 0f else -3f,
        targetValue = if (isDimmed) 0f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_wing"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f + bobOffset

            // Draw Radiant Sun / Fire Aura (softer and dimmer if inactive)
            drawAuraGlow(centerX, centerY, canvasW * 0.46f * breathScale, glowAlpha, stage, isDimmed)

            // Draw Stage-specific character with dimming alpha layer if inactive
            drawContext.canvas.saveLayer(
                androidx.compose.ui.geometry.Rect(0f, 0f, canvasW, canvasH),
                androidx.compose.ui.graphics.Paint().apply {
                    alpha = if (isDimmed) 0.58f else 1.0f
                }
            )

            when (stage) {
                BennuStage.EGG -> drawEgg(centerX, centerY, canvasW, breathScale)
                BennuStage.CHICK -> drawChick(centerX, centerY, canvasW, breathScale, celebrationWingAngle + idleWingFlap)
                BennuStage.YOUNG_BIRD -> drawYoungBird(centerX, centerY, canvasW, breathScale, celebrationWingAngle + idleWingFlap)
                BennuStage.RADIANT_PHOENIX -> drawRadiantPhoenix(centerX, centerY, canvasW, breathScale, celebrationWingAngle + idleWingFlap)
            }

            drawContext.canvas.restore()
        }
    }
}

private fun DrawScope.drawAuraGlow(centerX: Float, centerY: Float, radius: Float, alpha: Float, stage: BennuStage, isDimmed: Boolean) {
    val auraColor = if (isDimmed) {
        Color(0xFF9E9E9E)
    } else {
        when (stage) {
            BennuStage.EGG -> Color(0xFFFFD54F)
            BennuStage.CHICK -> Color(0xFFFFB300)
            BennuStage.YOUNG_BIRD -> Color(0xFFFF9100)
            BennuStage.RADIANT_PHOENIX -> Color(0xFFFF6F00)
        }
    }

    val effectiveAlpha = if (isDimmed) alpha * 0.45f else alpha

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = effectiveAlpha),
                auraColor.copy(alpha = effectiveAlpha * 0.35f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = radius * (if (isDimmed) 1.1f else 1.4f)
        ),
        radius = radius * (if (isDimmed) 1.1f else 1.4f),
        center = Offset(centerX, centerY)
    )
}

private fun DrawScope.drawEgg(cx: Float, cy: Float, totalSize: Float, scale: Float) {
    val eggWidth = totalSize * 0.44f * scale
    val eggHeight = totalSize * 0.58f * scale

    val eggPath = Path().apply {
        moveTo(cx, cy - eggHeight / 2f)
        cubicTo(
            cx + eggWidth / 2f, cy - eggHeight * 0.3f,
            cx + eggWidth / 2f, cy + eggHeight * 0.4f,
            cx, cy + eggHeight / 2f
        )
        cubicTo(
            cx - eggWidth / 2f, cy + eggHeight * 0.4f,
            cx - eggWidth / 2f, cy - eggHeight * 0.3f,
            cx, cy - eggHeight / 2f
        )
        close()
    }

    // Glowing golden egg body gradient
    drawPath(
        path = eggPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFE65100)),
            startY = cy - eggHeight / 2f,
            endY = cy + eggHeight / 2f
        )
    )

    // Inner glowing sun crack lines
    val crackPath = Path().apply {
        moveTo(cx - eggWidth * 0.15f, cy - eggHeight * 0.15f)
        lineTo(cx + eggWidth * 0.05f, cy - eggHeight * 0.02f)
        lineTo(cx - eggWidth * 0.08f, cy + eggHeight * 0.12f)
        lineTo(cx + eggWidth * 0.18f, cy + eggHeight * 0.22f)
    }
    drawPath(
        path = crackPath,
        color = Color(0xFFFFFFFF),
        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Specular shine highlight
    drawOval(
        color = Color.White.copy(alpha = 0.55f),
        topLeft = Offset(cx - eggWidth * 0.28f, cy - eggHeight * 0.35f),
        size = Size(eggWidth * 0.2f, eggHeight * 0.14f)
    )
}

private fun DrawScope.drawChick(cx: Float, cy: Float, totalSize: Float, scale: Float, wingAngle: Float) {
    val bodyRadius = totalSize * 0.22f * scale
    val headRadius = totalSize * 0.16f * scale
    val headCenterY = cy - bodyRadius * 0.65f

    // Little wings
    val leftWingPath = Path().apply {
        moveTo(cx - bodyRadius * 0.8f, cy)
        quadraticTo(cx - bodyRadius * 1.35f - wingAngle, cy - bodyRadius * 0.2f - wingAngle, cx - bodyRadius * 0.6f, cy + bodyRadius * 0.5f)
        close()
    }
    val rightWingPath = Path().apply {
        moveTo(cx + bodyRadius * 0.8f, cy)
        quadraticTo(cx + bodyRadius * 1.35f + wingAngle, cy - bodyRadius * 0.2f - wingAngle, cx + bodyRadius * 0.6f, cy + bodyRadius * 0.5f)
        close()
    }

    drawPath(leftWingPath, Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF8F00))))
    drawPath(rightWingPath, Brush.horizontalGradient(listOf(Color(0xFFFF8F00), Color(0xFFFFB300))))

    // Round body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFFF6F00)),
            center = Offset(cx, cy),
            radius = bodyRadius
        ),
        radius = bodyRadius,
        center = Offset(cx, cy)
    )

    // Head
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFCA28), Color(0xFFFF8F00)),
            center = Offset(cx, headCenterY),
            radius = headRadius
        ),
        radius = headRadius,
        center = Offset(cx, headCenterY)
    )

    // Feather tuft crest on top
    val crestPath = Path().apply {
        moveTo(cx - 8f, headCenterY - headRadius)
        quadraticTo(cx - 14f, headCenterY - headRadius - 22f, cx, headCenterY - headRadius - 28f)
        quadraticTo(cx + 14f, headCenterY - headRadius - 22f, cx + 8f, headCenterY - headRadius)
        close()
    }
    drawPath(crestPath, Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF6F00))))

    // Big shiny cute eyes
    val eyeOffsetX = headRadius * 0.42f
    val eyeOffsetY = headCenterY - headRadius * 0.08f
    val eyeRadius = headRadius * 0.22f

    drawCircle(Color(0xFF1E1B18), eyeRadius, Offset(cx - eyeOffsetX, eyeOffsetY))
    drawCircle(Color(0xFF1E1B18), eyeRadius, Offset(cx + eyeOffsetX, eyeOffsetY))
    // Eye shine
    drawCircle(Color.White, eyeRadius * 0.4f, Offset(cx - eyeOffsetX - 2f, eyeOffsetY - 2f))
    drawCircle(Color.White, eyeRadius * 0.4f, Offset(cx + eyeOffsetX - 2f, eyeOffsetY - 2f))

    // Cute beak
    val beakPath = Path().apply {
        moveTo(cx - 9f, eyeOffsetY + 8f)
        lineTo(cx + 9f, eyeOffsetY + 8f)
        lineTo(cx, eyeOffsetY + 22f)
        close()
    }
    drawPath(beakPath, Color(0xFFFF5722))
}

private fun DrawScope.drawYoungBird(cx: Float, cy: Float, totalSize: Float, scale: Float, wingAngle: Float) {
    val bodyW = totalSize * 0.38f * scale
    val bodyH = totalSize * 0.44f * scale

    // Elegant spread wings
    val leftWing = Path().apply {
        moveTo(cx - bodyW * 0.3f, cy)
        cubicTo(
            cx - bodyW * 1.1f - wingAngle, cy - bodyH * 0.6f - wingAngle,
            cx - bodyW * 1.3f - wingAngle, cy + bodyH * 0.1f,
            cx - bodyW * 0.35f, cy + bodyH * 0.4f
        )
        close()
    }
    val rightWing = Path().apply {
        moveTo(cx + bodyW * 0.3f, cy)
        cubicTo(
            cx + bodyW * 1.1f + wingAngle, cy - bodyH * 0.6f - wingAngle,
            cx + bodyW * 1.3f + wingAngle, cy + bodyH * 0.1f,
            cx + bodyW * 0.35f, cy + bodyH * 0.4f
        )
        close()
    }

    drawPath(leftWing, Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF6F00))))
    drawPath(rightWing, Brush.linearGradient(listOf(Color(0xFFFF6F00), Color(0xFFFFD54F))))

    // Sleek Torso
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFE65100)),
            startY = cy - bodyH * 0.4f,
            endY = cy + bodyH * 0.5f
        ),
        topLeft = Offset(cx - bodyW * 0.32f, cy - bodyH * 0.35f),
        size = Size(bodyW * 0.64f, bodyH * 0.8f)
    )

    // Noble Head
    val headY = cy - bodyH * 0.42f
    val headR = bodyW * 0.28f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFF9C4), Color(0xFFFFB300), Color(0xFFE65100)),
            center = Offset(cx, headY),
            radius = headR
        ),
        radius = headR,
        center = Offset(cx, headY)
    )

    // Crest Feathers (Egyptian Crown style Bennu plume)
    val plumePath = Path().apply {
        moveTo(cx - 6f, headY - headR)
        cubicTo(cx - 18f, headY - headR - 36f, cx + 8f, headY - headR - 44f, cx + 18f, headY - headR - 38f)
        cubicTo(cx + 4f, headY - headR - 26f, cx + 10f, headY - headR - 10f, cx + 4f, headY - headR)
        close()
    }
    drawPath(plumePath, Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF3D00))))

    // Eyes with intelligent spark
    val eyeY = headY - 4f
    drawCircle(Color(0xFF0F172A), 7f, Offset(cx - 14f, eyeY))
    drawCircle(Color(0xFF0F172A), 7f, Offset(cx + 14f, eyeY))
    drawCircle(Color(0xFFFFD54F), 3f, Offset(cx - 15f, eyeY - 2f))
    drawCircle(Color(0xFFFFD54F), 3f, Offset(cx + 13f, eyeY - 2f))

    // Sharp golden-orange beak
    val beak = Path().apply {
        moveTo(cx - 8f, eyeY + 6f)
        lineTo(cx + 8f, eyeY + 6f)
        lineTo(cx, eyeY + 24f)
        close()
    }
    drawPath(beak, Color(0xFFFF6D00))
}

private fun DrawScope.drawRadiantPhoenix(cx: Float, cy: Float, totalSize: Float, scale: Float, wingAngle: Float) {
    val bodyW = totalSize * 0.42f * scale
    val bodyH = totalSize * 0.48f * scale

    // Triple flame wings
    for (i in 0..2) {
        val layerOffset = i * 10f
        val wingAlpha = 1f - i * 0.2f
        val lWing = Path().apply {
            moveTo(cx - bodyW * 0.25f, cy + layerOffset)
            cubicTo(
                cx - bodyW * 1.35f - wingAngle - layerOffset, cy - bodyH * 0.85f - wingAngle + layerOffset,
                cx - bodyW * 1.55f - wingAngle, cy + bodyH * 0.2f,
                cx - bodyW * 0.35f, cy + bodyH * 0.5f
            )
            close()
        }
        val rWing = Path().apply {
            moveTo(cx + bodyW * 0.25f, cy + layerOffset)
            cubicTo(
                cx + bodyW * 1.35f + wingAngle + layerOffset, cy - bodyH * 0.85f - wingAngle + layerOffset,
                cx + bodyW * 1.55f + wingAngle, cy + bodyH * 0.2f,
                cx + bodyW * 0.35f, cy + bodyH * 0.5f
            )
            close()
        }

        val wingBrush = Brush.linearGradient(
            listOf(
                Color(0xFFFFD54F).copy(alpha = wingAlpha),
                Color(0xFFFF9100).copy(alpha = wingAlpha),
                Color(0xFFDD2C00).copy(alpha = wingAlpha)
            )
        )
        drawPath(lWing, wingBrush)
        drawPath(rWing, wingBrush)
    }

    // Radiant Flame Tail Feathers
    val tailPath = Path().apply {
        moveTo(cx - 18f, cy + bodyH * 0.45f)
        cubicTo(cx - 32f, cy + bodyH * 0.9f, cx - 12f, cy + bodyH * 1.15f, cx, cy + bodyH * 1.25f)
        cubicTo(cx + 12f, cy + bodyH * 1.15f, cx + 32f, cy + bodyH * 0.9f, cx + 18f, cy + bodyH * 0.45f)
        close()
    }
    drawPath(tailPath, Brush.verticalGradient(listOf(Color(0xFFFF9100), Color(0xFFFF3D00), Color(0x00FF3D00))))

    // Glowing Phoenix Torso
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFD84315)),
            startY = cy - bodyH * 0.4f,
            endY = cy + bodyH * 0.5f
        ),
        topLeft = Offset(cx - bodyW * 0.35f, cy - bodyH * 0.38f),
        size = Size(bodyW * 0.7f, bodyH * 0.85f)
    )

    // Sun Emblem on chest
    drawCircle(Color(0xFFFFF9C4), 10f, Offset(cx, cy))
    drawCircle(Color(0xFFFF6F00), 5f, Offset(cx, cy))

    // Crowned Head
    val headY = cy - bodyH * 0.46f
    val headR = bodyW * 0.3f
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFFFCA28), Color(0xFFE65100)),
            center = Offset(cx, headY),
            radius = headR
        ),
        radius = headR,
        center = Offset(cx, headY)
    )

    // Majestic Double Sun Crown Crest
    val crestL = Path().apply {
        moveTo(cx - 8f, headY - headR)
        cubicTo(cx - 28f, headY - headR - 46f, cx - 2f, headY - headR - 58f, cx + 6f, headY - headR - 48f)
        close()
    }
    val crestR = Path().apply {
        moveTo(cx + 8f, headY - headR)
        cubicTo(cx + 28f, headY - headR - 46f, cx + 2f, headY - headR - 58f, cx - 6f, headY - headR - 48f)
        close()
    }
    drawPath(crestL, Brush.verticalGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFF1744))))
    drawPath(crestR, Brush.verticalGradient(listOf(Color(0xFFFFEB3B), Color(0xFFFF1744))))

    // Luminous Eyes with golden fire
    val eyeY = headY - 5f
    drawCircle(Color(0xFF0F172A), 8f, Offset(cx - 16f, eyeY))
    drawCircle(Color(0xFF0F172A), 8f, Offset(cx + 16f, eyeY))
    drawCircle(Color(0xFFFFD54F), 4f, Offset(cx - 16f, eyeY - 2f))
    drawCircle(Color(0xFFFFD54F), 4f, Offset(cx + 16f, eyeY - 2f))
    drawCircle(Color.White, 2f, Offset(cx - 17f, eyeY - 3f))
    drawCircle(Color.White, 2f, Offset(cx + 15f, eyeY - 3f))

    // Golden Beak
    val beak = Path().apply {
        moveTo(cx - 10f, eyeY + 7f)
        lineTo(cx + 10f, eyeY + 7f)
        lineTo(cx, eyeY + 28f)
        close()
    }
    drawPath(beak, Color(0xFFFF3D00))
}
