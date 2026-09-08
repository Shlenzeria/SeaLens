package com.sealens.shared.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Cross-platform interactive surface helpers.
 *
 * On Windows Desktop the hover state is detected via pointer events; on Android
 * hover never fires so the state stays at the resting values.
 *
 * Colors switch instantly. No crossfade, spring, movement, or "follow/trail"
 * effect is used: those produced visible trailing/dimming when the cursor
 * swept across rows.
 */

class InteractiveSurfaceState internal constructor(
    val interactionSource: MutableInteractionSource,
    val surfaceColor: Color,
    val hoverModifier: Modifier,
)

/**
 * Returns surface values driven by hover and press (colors change instantly).
 *
 * @param restColor    resting background (no hover, no press)
 * @param hoverColor   background while pointer is over the surface
 * @param pressedColor background while pointer is held down
 */
@Composable
fun rememberInteractiveSurface(
    restColor: Color,
    hoverColor: Color,
    pressedColor: Color,
): InteractiveSurfaceState {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetColor = when {
        isPressed -> pressedColor
        isHovered -> hoverColor
        else -> restColor
    }

    val hoverModifier = Modifier.hoverable(
        interactionSource = interactionSource,
        enabled = true,
    )

    return InteractiveSurfaceState(
        interactionSource = interactionSource,
        surfaceColor = targetColor,
        hoverModifier = hoverModifier,
    )
}

/**
 * Small hover / press scale for any clickable surface.
 * Uses a 90 ms tween; because it is applied with graphicsLayer it never causes
 * a layout pass or affects neighbouring content.
 */
@Composable
fun Modifier.hoverScale(
    interactionSource: MutableInteractionSource,
    hoverScale: Float = 1.03f,
    pressedScale: Float = 0.98f,
): Modifier {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> pressedScale
            isHovered -> hoverScale
            else -> 1f
        },
        animationSpec = tween(durationMillis = 90),
        label = "buttonHoverScale",
    )
    return this
        .hoverable(interactionSource)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/** Filled M3 button with the shared hover / press scale. */
@Composable
fun ScaleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.hoverScale(interactionSource),
        content = content,
    )
}

/** M3 text button with the shared hover / press scale. */
@Composable
fun ScaleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    TextButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.hoverScale(interactionSource),
    ) {
        if (textColor != null) {
            Text(
                text = text,
                color = textColor,
            )
        } else {
            Text(text)
        }
    }
}
