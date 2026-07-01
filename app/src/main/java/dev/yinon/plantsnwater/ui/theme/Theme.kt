package dev.yinon.plantsnwater.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF35693C),
    secondary = Color(0xFF586249),
    tertiary = Color(0xFF386667),
    background = Color(0xFFF8FAF4),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0E6D5)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF9BD48F),
    secondary = Color(0xFFC0C9AD),
    tertiary = Color(0xFFA0CFD0),
    background = Color(0xFF11140F),
    surface = Color(0xFF191D16),
    surfaceVariant = Color(0xFF42493B)
)

@Composable
fun PlantsNWaterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
