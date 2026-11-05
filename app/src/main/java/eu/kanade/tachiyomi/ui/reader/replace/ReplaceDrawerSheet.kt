package eu.kanade.tachiyomi.ui.reader.replace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FindReplace
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AdaptiveSheet
import eu.kanade.presentation.components.TabTitle
import eu.kanade.presentation.components.TabbedDialogContent
import eu.kanade.presentation.components.TabbedDialogPaddings
import eu.kanade.tachiyomi.data.replace.ReplaceColors
import eu.kanade.tachiyomi.data.replace.ReplaceFindEntry
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightEntry
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightType
import eu.kanade.tachiyomi.data.replace.ReplaceStore
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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
    val tabTitles = listOf(
        TabTitle.Icon(Icons.Outlined.FindReplace),
        TabTitle.Icon(Icons.Outlined.FormatColorFill),
        TabTitle.Icon(Icons.Outlined.Palette),
        TabTitle.Icon(Icons.Outlined.Code),
    )
    val pagerState = rememberPagerState { tabTitles.size }

    BoxWithConstraints {
        AdaptiveSheet(
            onDismissRequest = onDismissRequest,
            modifier = Modifier.heightIn(max = maxHeight * 0.85f),
        ) {
            TabbedDialogContent(tabTitles = tabTitles, pagerState = pagerState) { page ->
                Column(
                    modifier = Modifier
                        .height(maxHeight * 0.6f)
                        .padding(vertical = TabbedDialogPaddings.Vertical),
                ) {
                    when (page) {
                        0 -> FindReplaceTab(store, viewModel)
                        1 -> HighlightTab(store, viewModel)
                        2 -> ColorsTab(colors, viewModel)
                        3 -> JsonTab(store, colors, viewModel, onExportJson)
                    }
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Profile", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
        Box {
            TextButton(onClick = { menuExpanded = true }) {
                Text(store.active, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
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
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Tambah profile") },
                    leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    onClick = {
                        nameInput = ""
                        showAddDialog = true
                        menuExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Rename profile") },
                    leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null) },
                    onClick = {
                        nameInput = store.active
                        showRenameDialog = true
                        menuExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Hapus profile") },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    enabled = names.size > 1,
                    onClick = {
                        viewModel.deleteProfile(store.active)
                        menuExpanded = false
                    },
                )
            }
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
    AlertDialog(
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

    val filteredWithIndex = remember(items.toList(), searchQuery) {
        if (searchQuery.isBlank()) {
            items.mapIndexed { index, entry -> index to entry }
        } else {
            items.mapIndexedNotNull { index, entry ->
                if (entry.find.contains(searchQuery, ignoreCase = true) ||
                    entry.replace.contains(searchQuery, ignoreCase = true)
                ) {
                    index to entry
                } else {
                    null
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        ProfileBar(store, viewModel)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
            CompactField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "cari...",
                modifier = Modifier.fillMaxWidth(),
            )
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
            items(filteredWithIndex, key = { it.second.id }) { (realIndex, entry) ->
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

    val filteredWithIndex = remember(items.toList(), searchQuery) {
        if (searchQuery.isBlank()) {
            items.mapIndexed { index, entry -> index to entry }
        } else {
            items.mapIndexedNotNull { index, entry ->
                if (entry.text.contains(searchQuery, ignoreCase = true)) {
                    index to entry
                } else {
                    null
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        ProfileBar(store, viewModel)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CompactField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = "teks untuk di-highlight",
                modifier = Modifier.fillMaxWidth(),
            )
            CompactField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "cari...",
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
            items(filteredWithIndex, key = { it.second.id }) { (realIndex, entry) ->
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
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
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
        Checkbox(
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
            Checkbox(
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
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
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

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text("Palette", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ReplaceColors.palettePresets.forEach { preset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setColor(ReplaceColorKey.CHAR, preset.char)
                            viewModel.setColor(ReplaceColorKey.SKILL, preset.skill)
                            viewModel.setColor(ReplaceColorKey.PLACE, preset.place)
                            viewModel.setColor(ReplaceColorKey.QUOTE, preset.quote)
                            viewModel.setColor(ReplaceColorKey.BRACKET, preset.bracket)
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        preset.name,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.width(72.dp),
                    )
                    val gradientColors = remember(preset) {
                        listOf(preset.char, preset.skill, preset.place, preset.quote, preset.bracket)
                            .map { hex -> runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray) }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .background(
                                Brush.horizontalGradient(gradientColors),
                                shape = MaterialTheme.shapes.small,
                            ),
                    )
                }
            }
        }
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
        Text(
            label,
            modifier = Modifier.width(88.dp),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
        Surface(
            onClick = onSwatchClick,
            color = color,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .width(40.dp)
                .height(28.dp),
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
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JsonAction(
                icon = Icons.Outlined.Refresh,
                label = "Refresh",
                onClick = {
                    textValue = json.encodeToString(ReplaceStore.serializer(), store)
                    message = ""
                },
            )
            JsonAction(
                icon = Icons.Outlined.FileOpen,
                label = "Import",
                onClick = {
                    importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
            )
            JsonAction(
                icon = Icons.Outlined.CheckCircle,
                label = "Apply",
                onClick = {
                    runCatching {
                        val newStore = json.decodeFromString(ReplaceStore.serializer(), textValue)
                        viewModel.replaceStore(newStore)
                        message = "Import OK (overwrite)"
                    }.onFailure { e ->
                        message = "Import gagal: ${e.message ?: "format salah"}"
                    }
                },
            )
            JsonAction(
                icon = Icons.Outlined.ContentCopy,
                label = "Copy",
                onClick = {
                    val payload = json.encodeToString(ReplaceStore.serializer(), store)
                    onExportJson(payload)
                    message = "Export dikirim (clipboard)"
                },
            )
            JsonAction(
                icon = Icons.Outlined.Save,
                label = "Save",
                onClick = {
                    exportLauncher.launch("nekori-replace.json")
                },
            )
        }

        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
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

@Composable
private fun JsonAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
