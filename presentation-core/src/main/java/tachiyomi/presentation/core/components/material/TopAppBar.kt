package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TopAppBar with the same sharp, boxy look as the bottom NavigationBar:
 * full-width, 2dp rounded corners, 1dp outline border, subtle shadow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: androidx.compose.ui.unit.Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: androidx.compose.foundation.layout.WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Surface(
        color = colors.containerColor,
        contentColor = colors.titleContentColor,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        androidx.compose.material3.TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior,
        )
    }
}

/**
 * Small top app bar with the same boxy treatment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: androidx.compose.foundation.layout.WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    Surface(
        color = colors.containerColor,
        contentColor = colors.titleContentColor,
        tonalElevation = 0.dp,
        shadowElevation = 3.dp,
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        androidx.compose.material3.TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior,
        )
    }
}
