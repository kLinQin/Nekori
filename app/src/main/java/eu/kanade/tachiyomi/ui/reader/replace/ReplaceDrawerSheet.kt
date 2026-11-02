package eu.kanade.tachiyomi.ui.reader.replace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.data.replace.ReplaceColors
import eu.kanade.tachiyomi.data.replace.ReplaceFindEntry
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightEntry
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightType
import eu.kanade.tachiyomi.data.replace.ReplaceProfile
import eu.kanade.tachiyomi.data.replace.ReplaceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val HEX_REGEX = Regex("^#[0-9a-fA-F]{6}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplaceDrawerSheet(
    store: ReplaceStore,
    colors: ReplaceColors,
    viewModel: ReplaceViewModel,
    onDismissRequest: () -> Unit,
    onExportJson: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("F&R", "Highlight", "Colors", "JSON")

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(selectedTabIndex = activeTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
            ) {
                when (activeTab) {
                    0 -> FindReplaceTab(store, viewModel)
                    1 -> HighlightTab(store, viewModel)
                    2 -> ColorsTab(colors, viewModel)
                    3 -> JsonTab(store, colors, viewModel, onExportJson)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
@Composable
private fun ProfileBar(
    store: ReplaceStore,
    viewModel: ReplaceViewModel,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }

    val names = store.list.keys.toList()
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("Profile:", style = MaterialTheme.typography.labelLarge)
        Surface(
            onClick = { menuExpanded = true },
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(store.active, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(" ▾")
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            names.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        viewModel.setActiveProfile(name)
                        menuExpanded = false
                    },
                )
            }
        }
        IconButton(onClick = {
            nameInput = ""
            showAddDialog = true
        }) {
            Icon(Icons.Outlined.Add, contentDescription = "Tambah profile")
        }
        IconButton(onClick = {
            nameInput = store.active
            showRenameDialog = true
        }) {
            Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = "Rename profile")
        }
        IconButton(
            onClick = { viewModel.deleteProfile(store.active) },
            enabled = names.size > 1,
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = "Hapus profile")
        }
    }

    if (showAddDialog) {
        TextInputDialog(
            title = "Nama profile baru",
            initial = nameInput,
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addProfile(name)
                showAddDialog = false
            },
        )
    }
    if (showRenameDialog) {
        TextInputDialog(
            title = "Nama baru",
            initial = nameInput,
            onDismiss = { showRenameDialog = false },
            onConfirm = { name ->
                viewModel.renameProfile(store.active, name)
                showRenameDialog = false
            },
        )
    }
}

@Composable
private fun TextInputDialog(
    title: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = value.trim()
                    if (trimmed.isNotEmpty()) onConfirm(trimmed)
                },
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FindReplaceTab(
    store: ReplaceStore,
    viewModel: ReplaceViewModel,
) {
    val profile = store.activeProfile()
    val items = remember(profile.fr) { profile.fr.toMutableStateList() }

    var findInput by remember { mutableStateOf("") }
    var replaceInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<ReplaceHighlightType?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(items.toList(), searchQuery) {
        if (searchQuery.isBlank()) items.toList()
        else items.filter {
            it.find.contains(searchQuery, ignoreCase = true) ||
                it.replace.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileBar(store, viewModel)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CompactField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "cari...",
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CompactField(
                    value = findInput,
                    onValueChange = { findInput = it },
                    placeholder = "find",
                    modifier = Modifier.weight(1f),
                )
                CompactField(
                    value = replaceInput,
                    onValueChange = { replaceInput = it },
                    placeholder = "replace",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TypeChip("None", selectedType == null) { selectedType = null }
                ReplaceHighlightType.entries.forEach { type ->
                    TypeChip(
                        label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedType == type,
                    ) { selectedType = type }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    val find = findInput.trim()
                    if (find.isEmpty()) return@TextButton
                    viewModel.addFindEntry(find, replaceInput)
                    if (selectedType != null && replaceInput.isNotBlank()) {
                        viewModel.addHighlightEntry(replaceInput, selectedType!!)
                    }
                    findInput = ""
                    replaceInput = ""
                    selectedType = null
                }) { Text("Add") }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            itemsIndexed(filtered) { filteredIndex, entry ->
                val realIndex = items.indexOf(entry)
                CompactFindRow(
                    entry = entry,
                    canMoveUp = realIndex > 0,
                    canMoveDown = realIndex < items.size - 1,
                    onMoveUp = {
                        if (realIndex > 0) {
                            val moved = items.removeAt(realIndex)
                            items.add(realIndex - 1, moved)
                            viewModel.moveFindEntry(realIndex, realIndex - 1)
                        }
                    },
                    onMoveDown = {
                        if (realIndex < items.size - 1) {
                            val moved = items.removeAt(realIndex)
                            items.add(realIndex + 1, moved)
                            viewModel.moveFindEntry(realIndex, realIndex + 1)
                        }
                    },
                    onToggle = { checked ->
                        viewModel.updateFindEntry(realIndex, entry.copy(enabled = checked))
                    },
                    onFindChange = {
                        viewModel.updateFindEntry(realIndex, entry.copy(find = it))
                    },
                    onReplaceChange = {
                        viewModel.updateFindEntry(realIndex, entry.copy(replace = it))
                    },
                    onDelete = { viewModel.deleteFindEntry(realIndex) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HighlightTab(
    store: ReplaceStore,
    viewModel: ReplaceViewModel,
) {
    val profile = store.activeProfile()
    val items = remember(profile.hl) { profile.hl.toMutableStateList() }

    var textInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ReplaceHighlightType.CHAR) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(items.toList(), searchQuery) {
        if (searchQuery.isBlank()) items.toList()
        else items.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ProfileBar(store, viewModel)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CompactField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "cari...",
                modifier = Modifier.fillMaxWidth(),
            )
            CompactField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = "teks untuk di-highlight",
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReplaceHighlightType.entries.forEach { type ->
                    TypeChip(
                        label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = selectedType == type,
                    ) { selectedType = type }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    val text = textInput.trim()
                    if (text.isEmpty()) return@TextButton
                    viewModel.addHighlightEntry(text, selectedType)
                    textInput = ""
                }) { Text("Add") }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            itemsIndexed(filtered) { filteredIndex, entry ->
                val realIndex = items.indexOf(entry)
                CompactHighlightRow(
                    entry = entry,
                    canMoveUp = realIndex > 0,
                    canMoveDown = realIndex < items.size - 1,
                    onMoveUp = {
                        if (realIndex > 0) {
                            val moved = items.removeAt(realIndex)
                            items.add(realIndex - 1, moved)
                            viewModel.moveHighlightEntry(realIndex, realIndex - 1)
                        }
                    },
                    onMoveDown = {
                        if (realIndex < items.size - 1) {
                            val moved = items.removeAt(realIndex)
                            items.add(realIndex + 1, moved)
                            viewModel.moveHighlightEntry(realIndex, realIndex + 1)
                        }
                    },
                    onToggle = { checked ->
                        viewModel.updateHighlightEntry(realIndex, entry.copy(enabled = checked))
                    },
                    onTextChange = {
                        viewModel.updateHighlightEntry(realIndex, entry.copy(text = it))
                    },
                    onTypeChange = { type ->
                        viewModel.updateHighlightEntry(realIndex, entry.copy(type = type))
                    },
                    onDelete = { viewModel.deleteHighlightEntry(realIndex) },
                )
            }
        }
    }
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraSmall,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                inner()
            },
        )
    }
}

@Composable
private fun CompactFindRow(
    entry: ReplaceFindEntry,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onFindChange: (String) -> Unit,
    onReplaceChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Naik", modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Turun", modifier = Modifier.size(16.dp))
            }
        }
        androidx.compose.material3.Checkbox(
            checked = entry.enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.size(32.dp),
        )
        CompactField(
            value = entry.find,
            onValueChange = onFindChange,
            placeholder = "find",
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(4.dp))
        CompactField(
            value = entry.replace,
            onValueChange = onReplaceChange,
            placeholder = "replace",
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Delete, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CompactHighlightRow(
    entry: ReplaceHighlightEntry,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onTypeChange: (ReplaceHighlightType) -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = "Naik", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Turun", modifier = Modifier.size(16.dp))
                }
            }
            androidx.compose.material3.Checkbox(
                checked = entry.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.size(32.dp),
            )
            CompactField(
                value = entry.text,
                onValueChange = onTextChange,
                placeholder = "teks",
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
            }
        }
        Row(
            modifier = Modifier.padding(start = 54.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ReplaceHighlightType.entries.forEach { type ->
                TypeChip(
                    label = type.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = entry.type == type,
                ) { onTypeChange(type) }
            }
        }
    }
}

@Composable
private fun ColorsTab(
    colors: ReplaceColors,
    viewModel: ReplaceViewModel,
) {
    var pickerKey by remember { mutableStateOf<ReplaceColorKey?>(null) }
    var pickerInitial by remember { mutableStateOf("#ffffff") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReplaceColorRow(
            label = "Character",
            hex = colors.char,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.CHAR
                pickerInitial = colors.char
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.CHAR, it) },
        )
        ReplaceColorRow(
            label = "Skill",
            hex = colors.skill,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.SKILL
                pickerInitial = colors.skill
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.SKILL, it) },
        )
        ReplaceColorRow(
            label = "Place",
            hex = colors.place,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.PLACE
                pickerInitial = colors.place
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.PLACE, it) },
        )
        ReplaceColorRow(
            label = "Quote",
            hex = colors.quote,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.QUOTE
                pickerInitial = colors.quote
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.QUOTE, it) },
        )
        ReplaceColorRow(
            label = "Bracket",
            hex = colors.bracket,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.BRACKET
                pickerInitial = colors.bracket
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.BRACKET, it) },
        )
        ReplaceColorRow(
            label = "Replace",
            hex = colors.replaceText,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.REPLACE_TEXT
                pickerInitial = colors.replaceText
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.REPLACE_TEXT, it) },
        )
        ReplaceColorRow(
            label = "Find active",
            hex = colors.findActive,
            onSwatchClick = {
                pickerKey = ReplaceColorKey.FIND_ACTIVE
                pickerInitial = colors.findActive
            },
            onHexChange = { if (HEX_REGEX.matches(it)) viewModel.setColor(ReplaceColorKey.FIND_ACTIVE, it) },
        )
    }

    val key = pickerKey
    if (key != null) {
        ReplaceColorPickerDialog(
            initial = pickerInitial,
            onDismiss = { pickerKey = null },
            onPick = { hex ->
                viewModel.setColor(key, hex)
                pickerKey = null
            },
        )
    }
}

@Composable
private fun ReplaceColorRow(
    label: String,
    hex: String,
    onSwatchClick: () -> Unit,
    onHexChange: (String) -> Unit,
) {
    val color = remember(hex) {
        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
    }
    var hexField by remember(hex) { mutableStateOf(hex) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Surface(
            onClick = onSwatchClick,
            color = color,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .width(48.dp)
                .height(32.dp),
        ) {}
        OutlinedTextField(
            value = hexField,
            onValueChange = { value ->
                hexField = value
                if (HEX_REGEX.matches(value)) onHexChange(value)
            },
            singleLine = true,
            modifier = Modifier.width(120.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JsonTab(
    store: ReplaceStore,
    colors: ReplaceColors,
    viewModel: ReplaceViewModel,
    onExportJson: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val json = remember { Json { prettyPrint = true; encodeDefaults = true; ignoreUnknownKeys = true } }

    var textValue by remember { mutableStateOf(json.encodeToString(ReplaceStore.serializer(), store)) }
    var message by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
            }
            if (content == null) {
                message = "Gagal baca file"
                return@launch
            }
            textValue = content
            message = "File dimuat. Tekan Import untuk menerapkan."
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(textValue) }
                    true
                }.getOrDefault(false)
            }
            message = if (ok) "Tersimpan ke file" else "Gagal menyimpan file"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                textValue = json.encodeToString(ReplaceStore.serializer(), store)
                message = ""
            }) { Text("Refresh") }
            TextButton(onClick = {
                importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            }) { Text("Import file") }
            TextButton(onClick = {
                runCatching {
                    val newStore = json.decodeFromString(ReplaceStore.serializer(), textValue)
                    viewModel.replaceStore(newStore)
                    message = "Import OK (overwrite)"
                }.onFailure { e ->
                    message = "Import gagal: ${e.message ?: "format salah"}"
                }
            }) { Text("Import (overwrite)") }
            TextButton(onClick = {
                val payload = json.encodeToString(ReplaceStore.serializer(), store)
                onExportJson(payload)
                message = "Export dikirim (clipboard)"
            }) { Text("Export (copy)") }
            TextButton(onClick = {
                exportLauncher.launch("nekori-replace.json")
            }) { Text("Export file") }
        }

        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            ),
        )

        if (message.isNotEmpty()) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
