package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Soft pastel pink theme inspired by cherry blossoms.
 */
internal object SakuraColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFF5B8CB),
        onPrimary = Color(0xFF2A1018),
        primaryContainer = Color(0xFFF5B8CB),
        onPrimaryContainer = Color(0xFF2A1018),
        secondary = Color(0xFFD9A0B8),
        onSecondary = Color(0xFF2A1018),
        secondaryContainer = Color(0xFF3A202A),
        onSecondaryContainer = Color(0xFFF5D5E0),
        tertiary = Color(0xFFC9B8E0),
        onTertiary = Color(0xFF1F1030),
        tertiaryContainer = Color(0xFF2A2035),
        onTertiaryContainer = Color(0xFFE0D5F0),
        error = Color(0xFFE88B9B),
        onError = Color(0xFF2A1015),
        errorContainer = Color(0xFF5C1C28),
        onErrorContainer = Color(0xFFF0B5C0),
        background = Color(0xFF1A1215),
        onBackground = Color(0xFFF0E0E5),
        surface = Color(0xFF1A1215),
        onSurface = Color(0xFFF0E0E5),
        surfaceVariant = Color(0xFF2E2228),
        onSurfaceVariant = Color(0xFFF0E0E5),
        outline = Color(0xFFF5B8CB),
        outlineVariant = Color(0xFF5A4048),
        scrim = Color(0xFF10080A),
        inverseSurface = Color(0xFFFDF2F5),
        inverseOnSurface = Color(0xFF3D2A30),
        inversePrimary = Color(0xFFD98098),
        surfaceDim = Color(0xFF1A1215),
        surfaceBright = Color(0xFF3A2A30),
        surfaceContainerLowest = Color(0xFF1A1215),
        surfaceContainerLow = Color(0xFF2A1E22),
        surfaceContainer = Color(0xFF2A1E22),
        surfaceContainerHigh = Color(0xFF2A1E22),
        surfaceContainerHighest = Color(0xFF3A2A30),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFD98098),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD98098),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFC08098),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFADDE5),
        onSecondaryContainer = Color(0xFF3D2A30),
        tertiary = Color(0xFFA89BC0),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFE5DDF0),
        onTertiaryContainer = Color(0xFF2A2035),
        error = Color(0xFFC05C6E),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF0D0D5),
        onErrorContainer = Color(0xFF5C1C28),
        background = Color(0xFFFDF2F5),
        onBackground = Color(0xFF3D2A30),
        surface = Color(0xFFFDF2F5),
        onSurface = Color(0xFF3D2A30),
        surfaceVariant = Color(0xFFF5E5EA),
        onSurfaceVariant = Color(0xFF3D2A30),
        outline = Color(0xFFD98098),
        outlineVariant = Color(0xFFE0C0C9),
        scrim = Color(0xFF2A1E22),
        inverseSurface = Color(0xFF2A1E22),
        inverseOnSurface = Color(0xFFF0E0E5),
        inversePrimary = Color(0xFFF5B8CB),
        surfaceDim = Color(0xFFFDF2F5),
        surfaceBright = Color(0xFFF0E0E5),
        surfaceContainerLowest = Color(0xFFFDF2F5),
        surfaceContainerLow = Color(0xFFF5E5EA),
        surfaceContainer = Color(0xFFF5E5EA),
        surfaceContainerHigh = Color(0xFFF5E5EA),
        surfaceContainerHighest = Color(0xFFFADDE5),
    )
}
