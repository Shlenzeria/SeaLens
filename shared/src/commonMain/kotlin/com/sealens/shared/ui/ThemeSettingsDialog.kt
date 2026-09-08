package com.sealens.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sealens.shared.theme.Palette
import com.sealens.shared.theme.SeaLensColorMode
import com.sealens.shared.theme.SeaLensTheme
import com.sealens.shared.theme.SeaLensThemeChoice
import com.sealens.shared.theme.paletteFor

/**
 * Theme settings dialog: pick one of 5 Morandi themes, and Light / Dark.
 */
@Composable
fun ThemeSettingsDialog(
    currentTheme: SeaLensThemeChoice,
    currentMode: SeaLensColorMode,
    onPickTheme: (SeaLensThemeChoice) -> Unit,
    onPickMode: (SeaLensColorMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        title = { Text("主题设置") },
        text = {
            Column {
                Text(
                    text = "浅色与深色都是低饱和的 Morandi 色系；切换会立刻生效并保存到本地。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "色调",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeaLensThemeChoice.values().forEach { choice ->
                        ThemeRow(
                            choice = choice,
                            selected = choice == currentTheme,
                            mode = currentMode,
                            onClick = { onPickTheme(choice) },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "明暗",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ModeChip(
                        label = "浅色",
                        selected = currentMode == SeaLensColorMode.Light,
                        onClick = { onPickMode(SeaLensColorMode.Light) },
                        modifier = Modifier.weight(1f),
                    )
                    ModeChip(
                        label = "深色",
                        selected = currentMode == SeaLensColorMode.Dark,
                        onClick = { onPickMode(SeaLensColorMode.Dark) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            ScaleTextButton(
                text = "完成",
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun ThemeRow(
    choice: SeaLensThemeChoice,
    selected: Boolean,
    mode: SeaLensColorMode,
    onClick: () -> Unit,
) {
    // Preview palette for THIS choice (not the currently-active one),
    // so the user sees what they're about to pick.
    val preview: Palette = remember(choice, mode) { paletteFor(choice, mode) }

    val interactive = rememberInteractiveSurface(
        restColor = if (selected) {
            SeaLensTheme.extras.selected
        } else {
            MaterialTheme.colorScheme.surface
        },
        hoverColor = if (selected) {
            SeaLensTheme.extras.selected
        } else {
            SeaLensTheme.extras.hover
        },
        pressedColor = SeaLensTheme.extras.pressed,
    )
    val textColor =
        if (selected) SeaLensTheme.extras.selectedText
        else MaterialTheme.colorScheme.onSurface
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactive.hoverModifier)
            .background(color = interactive.surfaceColor, shape = RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(
                onClick = onClick,
                interactionSource = interactive.interactionSource,
                indication = null,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .hoverScale(interactive.interactionSource),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SwatchDots(preview)
        Spacer(Modifier.width(12.dp))
        Text(
            text = themeLabel(choice),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
        Spacer(Modifier.weight(1f))
        if (selected) {
            Text(
                text = "当前",
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
            )
        }
    }
}

@Composable
private fun SwatchDots(palette: Palette) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Dot(palette.background)
        Dot(palette.surface)
        Dot(palette.primary)
        Dot(palette.primaryContainer)
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(color = color, shape = CircleShape)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), CircleShape),
    )
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactive = rememberInteractiveSurface(
        restColor = if (selected) {
            SeaLensTheme.extras.selected
        } else {
            MaterialTheme.colorScheme.surface
        },
        hoverColor = if (selected) {
            SeaLensTheme.extras.selected
        } else {
            SeaLensTheme.extras.hover
        },
        pressedColor = SeaLensTheme.extras.pressed,
    )
    val textColor =
        if (selected) SeaLensTheme.extras.selectedText
        else MaterialTheme.colorScheme.onSurface
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .then(interactive.hoverModifier)
            .background(color = interactive.surfaceColor, shape = RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(
                onClick = onClick,
                interactionSource = interactive.interactionSource,
                indication = null,
            )
            .padding(vertical = 12.dp)
            .hoverScale(interactive.interactionSource),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

private fun themeLabel(choice: SeaLensThemeChoice): String = when (choice) {
    SeaLensThemeChoice.BlueGray   -> "蓝灰（默认）"
    SeaLensThemeChoice.GrayPurple -> "灰紫"
    SeaLensThemeChoice.GrayGreen  -> "灰绿"
    SeaLensThemeChoice.GrayCyan   -> "灰青"
    SeaLensThemeChoice.GrayBrown  -> "灰棕"
}
