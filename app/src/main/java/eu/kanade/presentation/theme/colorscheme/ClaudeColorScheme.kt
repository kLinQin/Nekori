package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Warm earthy theme inspired by Anthropic Claude's aesthetic.
 */
internal object ClaudeColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE8855E),
        onPrimary = Color(0xFF2A1A10),
        primaryContainer = Color(0xFFE8855E),
        onPrimaryContainer = Color(0xFF2A1A10),
        secondary = Color(0xFFA88B5E),
        onSecondary = Color(0xFF2A1A10),
        secondaryContainer = Color(0xFF3A2E1F),
        onSecondaryContainer = Color(0xFFE8C9A0),
        tertiary = Color(0xFF8FA882),
        onTertiary = Color(0xFF14200F),
        tertiaryContainer = Color(0xFF2A2E24),
        onTertiaryContainer = Color(0xFFD8DDC9),
        error = Color(0xFFE88B8B),
        onError = Color(0xFF2A1010),
        errorContainer = Color(0xFF5C1C1C),
        onErrorContainer = Color(0xFFF0B5B5),
        background = Color(0xFF1F1B16),
        onBackground = Color(0xFFE8DDC9),
        surface = Color(0xFF1F1B16),
        onSurface = Color(0xFFE8DDC9),
        surfaceVariant = Color(0xFF2E2820),
        onSurfaceVariant = Color(0xFFE8DDC9),
        outline = Color(0xFFE8855E),
        outlineVariant = Color(0xFF5A4E3E),
        scrim = Color(0xFF100D08),
        inverseSurface = Color(0xFFF5F0E8),
        inverseOnSurface = Color(0xFF3D3529),
        inversePrimary = Color(0xFFD97757),
        surfaceDim = Color(0xFF1F1B16),
        surfaceBright = Color(0xFF3A3228),
        surfaceContainerLowest = Color(0xFF1F1B16),
        surfaceContainerLow = Color(0xFF2A241C),
        surfaceContainer = Color(0xFF2A241C),
        surfaceContainerHigh = Color(0xFF2A241C),
        surfaceContainerHighest = Color(0xFF3A3228),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFD97757),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD97757),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF8B6F47),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8D9C0),
        onSecondaryContainer = Color(0xFF3D3529),
        tertiary = Color(0xFF7B8F6E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD8DDC9),
        onTertiaryContainer = Color(0xFF2A3224),
        error = Color(0xFFB85C5C),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF0D0D0),
        onErrorContainer = Color(0xFF5C1C1C),
        background = Color(0xFFF5F0E8),
        onBackground = Color(0xFF3D3529),
        surface = Color(0xFFF5F0E8),
        onSurface = Color(0xFF3D3529),
        surfaceVariant = Color(0xFFEDE4D5),
        onSurfaceVariant = Color(0xFF3D3529),
        outline = Color(0xFFD97757),
        outlineVariant = Color(0xFFC9B99E),
        scrim = Color(0xFF2A241C),
        inverseSurface = Color(0xFF2A241C),
        inverseOnSurface = Color(0xFFE8DDC9),
        inversePrimary = Color(0xFFE8855E),
        surfaceDim = Color(0xFFF5F0E8),
        surfaceBright = Color(0xFFE8DDC9),
        surfaceContainerLowest = Color(0xFFF5F0E8),
        surfaceContainerLow = Color(0xFFEDE4D5),
        surfaceContainer = Color(0xFFEDE4D5),
        surfaceContainerHigh = Color(0xFFEDE4D5),
        surfaceContainerHighest = Color(0xFFE8D9C0),
    )
}
