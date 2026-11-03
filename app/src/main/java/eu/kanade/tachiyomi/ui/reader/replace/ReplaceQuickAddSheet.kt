package eu.kanade.tachiyomi.ui.reader.replace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightType

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReplaceQuickAddSheet(
    mode: ReplaceQuickAddMode,
    initialFind: String,
    initialReplace: String,
    viewModel: ReplaceViewModel,
    onDismissRequest: () -> Unit,
) {
    var findText by remember { mutableStateOf(initialFind) }
    var replaceText by remember { mutableStateOf(initialReplace) }
    var selectedType by remember {
        mutableStateOf<ReplaceHighlightType?>(if (mode == ReplaceQuickAddMode.HIGHLIGHT) ReplaceHighlightType.CHAR else null)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            Text(
                text = if (mode == ReplaceQuickAddMode.FIND_REPLACE) "Find & Replace" else "Highlight",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = findText,
                onValueChange = { findText = it },
                label = { Text(if (mode == ReplaceQuickAddMode.FIND_REPLACE) "find" else "teks untuk di-highlight") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (mode == ReplaceQuickAddMode.FIND_REPLACE) {
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("replace") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (mode == ReplaceQuickAddMode.FIND_REPLACE) {
                    QuickTypeChip("None", selectedType == null) { selectedType = null }
                }
                ReplaceHighlightType.entries.forEach { type ->
                    QuickTypeChip(
                        label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedType == type,
                    ) { selectedType = type }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) { Text("Batal") }
                Spacer(Modifier.padding(4.dp))
                TextButton(onClick = {
                    val find = findText.trim()
                    if (find.isEmpty()) return@TextButton
                    when (mode) {
                        ReplaceQuickAddMode.FIND_REPLACE -> {
                            viewModel.addFindEntry(find, replaceText)
                            val type = selectedType
                            if (type != null && replaceText.isNotBlank()) {
                                viewModel.addHighlightEntry(replaceText, type)
                            }
                        }
                        ReplaceQuickAddMode.HIGHLIGHT -> {
                            viewModel.addHighlightEntry(find, selectedType ?: ReplaceHighlightType.CHAR)
                        }
                    }
                        scope.launch {
                            sheetState.hide()
                            onDismissRequest()
                        }
                }) { Text("Add") }
            }
        }
    }
}

@Composable
private fun QuickTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

enum class ReplaceQuickAddMode { FIND_REPLACE, HIGHLIGHT }
