// DesignTokens.kt
// SeaLens unified design tokens (5 themes x 2 modes, low-saturation Morandi).
//
// Light-mode backgrounds are tinted with the theme's primary color so the page
// reads as one calm hue rather than grey-on-white. Dark-mode backgrounds are
// lifted slightly so the page is never pure black.

package com.sealens.shared.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

data class Palette(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
)

// BlueGray (default). Light page bg is the primary hue at ~10% tint so the
// whole surface feels like a single calm color.
private val BlueGrayLight = Palette(
    primary            = Color(0xFF657896), // accent / primary emphasis
    primaryContainer   = Color(0xFF97A8C4), // selected fill
    onPrimary          = Color(0xFFF5F8FC),
    onPrimaryContainer = Color(0xFF2B3850),
    background         = Color(0xFFB9C4DB), // page base color
    onBackground       = Color(0xFF323E52),
    surface            = Color(0xFFC8D1E3), // cards / editor surface
    onSurface          = Color(0xFF323E52),
    surfaceVariant     = Color(0xFFA9B6CF), // sidebar / panel (deeper than page)
    onSurfaceVariant   = Color(0xFF4D5A70),
    secondary          = Color(0xFF8491A8),
    onSecondary        = Color(0xFFF5F8FC),
    secondaryContainer = Color(0xFFC0CBDF),
    onSecondaryContainer = Color(0xFF323E52),
    outline            = Color(0xFF8E9CB4),
    outlineVariant     = Color(0xFFBEC9DC),
)

private val BlueGrayDark = Palette(
    primary            = Color(0xFF8FA1BC),
    primaryContainer   = Color(0xFF4E6079),
    onPrimary          = Color(0xFF1A2230),
    onPrimaryContainer = Color(0xFFD7DFEC),
    background         = Color(0xFF2B323E), // lifted, never pure black
    onBackground       = Color(0xFFCDD5E2),
    surface            = Color(0xFF343C49),
    onSurface          = Color(0xFFCDD5E2),
    surfaceVariant     = Color(0xFF252B36),
    onSurfaceVariant   = Color(0xFF98A3B5),
    secondary          = Color(0xFF7F8DA2),
    onSecondary        = Color(0xFF1A2230),
    secondaryContainer = Color(0xFF3E4958),
    onSecondaryContainer = Color(0xFFC3CCDB),
    outline            = Color(0xFF4B5565),
    outlineVariant     = Color(0xFF3C4553),
)

// Gray Purple.
private val GrayPurpleLight = Palette(
    primary            = Color(0xFF6F6384),
    primaryContainer   = Color(0xFF9B8EB6),
    onPrimary          = Color(0xFFF7F5FA),
    onPrimaryContainer = Color(0xFF2E2740),
    background         = Color(0xFFC1B5D0),
    onBackground       = Color(0xFF3A3348),
    surface            = Color(0xFFCFC5DC),
    onSurface          = Color(0xFF3A3348),
    surfaceVariant     = Color(0xFFAFA3C3),
    onSurfaceVariant   = Color(0xFF5B5271),
    secondary          = Color(0xFF9489A8),
    onSecondary        = Color(0xFFF7F5FA),
    secondaryContainer = Color(0xFFC7BDD9),
    onSecondaryContainer = Color(0xFF3A3348),
    outline            = Color(0xFF8B80A0),
    outlineVariant     = Color(0xFFC2B7D4),
)

private val GrayPurpleDark = Palette(
    primary            = Color(0xFFAC9FB9),
    primaryContainer   = Color(0xFF584A66),
    onPrimary          = Color(0xFF221A2D),
    onPrimaryContainer = Color(0xFFE2DCEA),
    background         = Color(0xFF352E40),
    onBackground       = Color(0xFFDCD5E3),
    surface            = Color(0xFF3F384B),
    onSurface          = Color(0xFFDCD5E3),
    surfaceVariant     = Color(0xFF2C2635),
    onSurfaceVariant   = Color(0xFFA39AAC),
    secondary          = Color(0xFF91879F),
    onSecondary        = Color(0xFF221A2D),
    secondaryContainer = Color(0xFF40394B),
    onSecondaryContainer = Color(0xFFCDC5D7),
    outline            = Color(0xFF564F62),
    outlineVariant     = Color(0xFF453E51),
)

// Gray Green.
private val GrayGreenLight = Palette(
    primary            = Color(0xFF6B7D65),
    primaryContainer   = Color(0xFF8FA58A),
    onPrimary          = Color(0xFFF6F8F5),
    onPrimaryContainer = Color(0xFF263129),
    background         = Color(0xFFB9C8B4),
    onBackground       = Color(0xFF333E31),
    surface            = Color(0xFFC8D3C2),
    onSurface          = Color(0xFF333E31),
    surfaceVariant     = Color(0xFFA7B9A1),
    onSurfaceVariant   = Color(0xFF55624F),
    secondary          = Color(0xFF8A9B84),
    onSecondary        = Color(0xFFF6F8F5),
    secondaryContainer = Color(0xFFC0CFBC),
    onSecondaryContainer = Color(0xFF333E31),
    outline            = Color(0xFF8A9C84),
    outlineVariant     = Color(0xFFBBC9B5),
)

private val GrayGreenDark = Palette(
    primary            = Color(0xFF9FB09D),
    primaryContainer   = Color(0xFF485749),
    onPrimary          = Color(0xFF182218),
    onPrimaryContainer = Color(0xFFDFE7DC),
    background         = Color(0xFF323A31),
    onBackground       = Color(0xFFD9DFD5),
    surface            = Color(0xFF3D453B),
    onSurface          = Color(0xFFD9DFD5),
    surfaceVariant     = Color(0xFF2A3029),
    onSurfaceVariant   = Color(0xFF9CA597),
    secondary          = Color(0xFF879680),
    onSecondary        = Color(0xFF182218),
    secondaryContainer = Color(0xFF3F463D),
    onSecondaryContainer = Color(0xFFCACFC6),
    outline            = Color(0xFF52594F),
    outlineVariant     = Color(0xFF40463E),
)

// Gray Cyan.
private val GrayCyanLight = Palette(
    primary            = Color(0xFF5C7B7E),
    primaryContainer   = Color(0xFF86A6AA),
    onPrimary          = Color(0xFFF6F9F9),
    onPrimaryContainer = Color(0xFF223337),
    background         = Color(0xFFB3C9CB),
    onBackground       = Color(0xFF2E3C3F),
    surface            = Color(0xFFC2D1D3),
    onSurface          = Color(0xFF2E3C3F),
    surfaceVariant     = Color(0xFFA0B9BC),
    onSurfaceVariant   = Color(0xFF4F6567),
    secondary          = Color(0xFF7E9A9C),
    onSecondary        = Color(0xFFF6F9F9),
    secondaryContainer = Color(0xFFB7CBCD),
    onSecondaryContainer = Color(0xFF2E3C3F),
    outline            = Color(0xFF829B9D),
    outlineVariant     = Color(0xFFB5C6C8),
)

private val GrayCyanDark = Palette(
    primary            = Color(0xFF98B0B2),
    primaryContainer   = Color(0xFF44585A),
    onPrimary          = Color(0xFF162324),
    onPrimaryContainer = Color(0xFFD9E6E7),
    background         = Color(0xFF303840),
    onBackground       = Color(0xFFD7DEDF),
    surface            = Color(0xFF3A434B),
    onSurface          = Color(0xFFD7DEDF),
    surfaceVariant     = Color(0xFF272E33),
    onSurfaceVariant   = Color(0xFF9BA6A8),
    secondary          = Color(0xFF7E9294),
    onSecondary        = Color(0xFF162324),
    secondaryContainer = Color(0xFF3C454B),
    onSecondaryContainer = Color(0xFFC7D2D3),
    outline            = Color(0xFF4F575B),
    outlineVariant     = Color(0xFF3E454A),
)

// Gray Brown (warm).
private val GrayBrownLight = Palette(
    primary            = Color(0xFF7A6A5B),
    primaryContainer   = Color(0xFF9C8C76),
    onPrimary          = Color(0xFFF9F6F2),
    onPrimaryContainer = Color(0xFF2E271E),
    background         = Color(0xFFC5B7A3),
    onBackground       = Color(0xFF3D352B),
    surface            = Color(0xFFD0C3B0),
    onSurface          = Color(0xFF3D352B),
    surfaceVariant     = Color(0xFFB3A48E),
    onSurfaceVariant   = Color(0xFF5D5143),
    secondary          = Color(0xFF9A8A78),
    onSecondary        = Color(0xFFF9F6F2),
    secondaryContainer = Color(0xFFC6B8A4),
    onSecondaryContainer = Color(0xFF3D352B),
    outline            = Color(0xFF8F8070),
    outlineVariant     = Color(0xFFC2B49E),
)

private val GrayBrownDark = Palette(
    primary            = Color(0xFFAC9C8E),
    primaryContainer   = Color(0xFF574840),
    onPrimary          = Color(0xFF221A14),
    onPrimaryContainer = Color(0xFFE3DACE),
    background         = Color(0xFF39322C),
    onBackground       = Color(0xFFDCD2C7),
    surface            = Color(0xFF433C35),
    onSurface          = Color(0xFFDCD2C7),
    surfaceVariant     = Color(0xFF2D2823),
    onSurfaceVariant   = Color(0xFFA1998E),
    secondary          = Color(0xFF958779),
    onSecondary        = Color(0xFF221A14),
    secondaryContainer = Color(0xFF423A33),
    onSecondaryContainer = Color(0xFFCFC4B7),
    outline            = Color(0xFF56504A),
    outlineVariant     = Color(0xFF44403B),
)

internal fun Palette.toColorScheme(): ColorScheme = if (isLightLike) {
    lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        error = Color(0xFF9A5C59),
        onError = Color(0xFFF8F1F0),
        errorContainer = Color(0xFFE0CCCA),
        onErrorContainer = Color(0xFF51312E),
        surfaceTint = primary,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = lerp(surface, surfaceVariant, 0.30f),
        surfaceContainerHigh = lerp(surface, surfaceVariant, 0.58f),
        surfaceContainerHighest = lerp(surfaceVariant, surface, 0.12f),
        inverseSurface = surfaceVariant,
        inverseOnSurface = onSurfaceVariant,
        inversePrimary = primaryContainer,
        scrim = Color(0x4D222B38),
    )
} else {
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        error = Color(0xFFD1A29E),
        onError = Color(0xFF321F1E),
        errorContainer = Color(0xFF5B3E3C),
        onErrorContainer = Color(0xFFE9D5D2),
        surfaceTint = primary,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = lerp(surface, surfaceVariant, 0.25f),
        surfaceContainerHigh = lerp(surface, surfaceVariant, 0.55f),
        surfaceContainerHighest = lerp(surfaceVariant, surface, 0.12f),
        inverseSurface = surface,
        inverseOnSurface = onSurface,
        inversePrimary = primaryContainer,
        scrim = Color(0x8F05070B),
    )
}

private val Palette.isLightLike: Boolean
    get() = background.luminance() > 0.5f

private fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    fun channel(v: Float): Float =
        if (v <= 0.03928f) v / 12.92f
        else Math.pow(((v + 0.055f) / 1.055f).toDouble(), 2.4).toFloat()
    return 0.2126f * channel(r) + 0.7152f * channel(g) + 0.0722f * channel(b)
}

enum class SeaLensThemeChoice {
    BlueGray,
    GrayPurple,
    GrayGreen,
    GrayCyan,
    GrayBrown,
}

enum class SeaLensColorMode { Light, Dark }

fun paletteFor(theme: SeaLensThemeChoice, mode: SeaLensColorMode): Palette =
    when (theme) {
        SeaLensThemeChoice.BlueGray   -> if (mode == SeaLensColorMode.Light) BlueGrayLight   else BlueGrayDark
        SeaLensThemeChoice.GrayPurple -> if (mode == SeaLensColorMode.Light) GrayPurpleLight else GrayPurpleDark
        SeaLensThemeChoice.GrayGreen  -> if (mode == SeaLensColorMode.Light) GrayGreenLight  else GrayGreenDark
        SeaLensThemeChoice.GrayCyan   -> if (mode == SeaLensColorMode.Light) GrayCyanLight   else GrayCyanDark
        SeaLensThemeChoice.GrayBrown  -> if (mode == SeaLensColorMode.Light) GrayBrownLight  else GrayBrownDark
    }
