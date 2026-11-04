package eu.kanade.tachiyomi.ui.reader.replace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ReplaceSelectionActions(
    modifier: Modifier = Modifier,
    onFindReplace: () -> Unit,
    onHighlight: () -> Unit,
    onTranslate: () -> Unit,
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
            ActionButton(Icons.Outlined.FindReplace, "Find & Replace", onFindReplace)
            ActionButton(Icons.Outlined.FormatColorFill, "Highlight", onHighlight)
        ActionButton(Icons.Outlined.Translate, "Translate", onTranslate)
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier.size(44.dp),
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}
