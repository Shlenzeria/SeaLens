package com.sealens.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Extra semantic tokens surfaced to all screens on top of MaterialTheme.colorScheme.
 *
 *  - selected         selection background (a stronger container than hover)
 *  - selectedText     text color when selected
 *  - hover            resting-row hover tint (sits between background and surfaceVariant)
 *  - pressed          slightly stronger than hover, used while pointer is held down
 *  - editorBackground / editorText: chapter editor surface
 *  - subtleDivider: hairline divider between dense regions
 *
 * Sourcing everything from the same Palette means swapping the theme / mode
 * in one place updates every screen instantly.
 */
data class SeaLensExtras(
    val selected: Color,
    val selectedText: Color,
    val hover: Color,
    val pressed: Color,
    val editorBackground: Color,
    val editorText: Color,
    val subtleDivider: Color,
)

val LocalSeaLensExtras = staticCompositionLocalOf<SeaLensExtras> {
    error("SeaLensExtras not provided; wrap your content in SeaLensTheme {}")
}

/** Convenience accessor: SeaLensTheme.extras.selected, .editorText, etc. */
object SeaLensTheme {
    val extras: SeaLensExtras
        @Composable
        @ReadOnlyComposable
        get() = LocalSeaLensExtras.current
}

@Composable
fun SeaLensTheme(
    themeChoice: SeaLensThemeChoice = SeaLensThemeChoice.BlueGray,
    colorMode: SeaLensColorMode = SeaLensColorMode.Light,
    content: @Composable () -> Unit,
) {
    val palette = paletteFor(themeChoice, colorMode)
    val colorScheme = palette.toColorScheme()
    val dark = colorMode == SeaLensColorMode.Dark

    // Hover/pressed are blended from the same palette so every theme stays in
    // its own low-saturation hue family. Nothing here is a spring or movement.
    val extras = SeaLensExtras(
        selected = palette.primaryContainer,
        selectedText = palette.onPrimaryContainer,
        hover = lerp(
            palette.surface,
            palette.primaryContainer,
            if (dark) 0.30f else 0.45f,
        ),
        pressed = lerp(
            palette.surface,
            palette.primary,
            if (dark) 0.25f else 0.35f,
        ),
        editorBackground = palette.surface,
        editorText = palette.onSurface,
        subtleDivider = palette.outlineVariant,
    )

    CompositionLocalProvider(LocalSeaLensExtras provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
