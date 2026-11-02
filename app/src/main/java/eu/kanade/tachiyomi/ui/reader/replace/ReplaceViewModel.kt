package eu.kanade.tachiyomi.ui.reader.replace

import androidx.lifecycle.ViewModel
import eu.kanade.tachiyomi.data.replace.ReplaceColors
import eu.kanade.tachiyomi.data.replace.ReplaceFindEntry
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightEntry
import eu.kanade.tachiyomi.data.replace.ReplaceHighlightType
import eu.kanade.tachiyomi.data.replace.ReplaceProfile
import eu.kanade.tachiyomi.data.replace.ReplaceRepository
import eu.kanade.tachiyomi.data.replace.ReplaceStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uy.kohesive.injekt.injectLazy

class ReplaceViewModel : ViewModel() {

    private val repository: ReplaceRepository by injectLazy()

    private val _store = MutableStateFlow(repository.getStore())
    val store: StateFlow<ReplaceStore> = _store.asStateFlow()

    private val _colors = MutableStateFlow(repository.getColors())
    val colors: StateFlow<ReplaceColors> = _colors.asStateFlow()

    private fun refreshStore() {
        _store.value = repository.getStore()
    }

    private fun refreshColors() {
        _colors.value = repository.getColors()
    }

    // Profile actions

    fun setActiveProfile(name: String) {
        repository.setActiveProfile(name)
        refreshStore()
    }

    fun addProfile(name: String) {
        repository.addProfile(name)
        refreshStore()
    }

    fun renameProfile(from: String, to: String) {
        repository.renameProfile(from, to)
        refreshStore()
    }

    fun deleteProfile(name: String) {
        repository.deleteProfile(name)
        refreshStore()
    }

    // Find & Replace entries

    fun addFindEntry(find: String, replace: String) {
        val findTrimmed = find.trim()
        if (findTrimmed.isEmpty()) return
        repository.updateActiveProfile { profile ->
            if (profile.fr.any { it.find.equals(findTrimmed, ignoreCase = true) }) return@updateActiveProfile profile
            profile.copy(fr = profile.fr + ReplaceFindEntry(findTrimmed, replace, true))
        }
        refreshStore()
    }

    fun updateFindEntry(index: Int, entry: ReplaceFindEntry) {
        repository.updateActiveProfile { profile ->
            if (index !in profile.fr.indices) return@updateActiveProfile profile
            val list = profile.fr.toMutableList()
            list[index] = entry
            profile.copy(fr = list)
        }
        refreshStore()
    }

    fun deleteFindEntry(index: Int) {
        repository.updateActiveProfile { profile ->
            if (index !in profile.fr.indices) return@updateActiveProfile profile
            profile.copy(fr = profile.fr.filterIndexed { i, _ -> i != index })
        }
        refreshStore()
    }

    fun moveFindEntry(from: Int, to: Int) {
        repository.updateActiveProfile { profile ->
            if (from !in profile.fr.indices || to !in profile.fr.indices || from == to) {
                return@updateActiveProfile profile
            }
            val list = profile.fr.toMutableList()
            val item = list.removeAt(from)
            list.add(to, item)
            profile.copy(fr = list)
        }
        refreshStore()
    }

    // Highlight entries

    fun addHighlightEntry(text: String, type: ReplaceHighlightType) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        repository.updateActiveProfile { profile ->
            if (profile.hl.any { it.text.equals(trimmed, ignoreCase = true) }) return@updateActiveProfile profile
            profile.copy(hl = profile.hl + ReplaceHighlightEntry(trimmed, type, true))
        }
        refreshStore()
    }

    fun updateHighlightEntry(index: Int, entry: ReplaceHighlightEntry) {
        repository.updateActiveProfile { profile ->
            if (index !in profile.hl.indices) return@updateActiveProfile profile
            val list = profile.hl.toMutableList()
            list[index] = entry
            profile.copy(hl = list)
        }
        refreshStore()
    }

    fun deleteHighlightEntry(index: Int) {
        repository.updateActiveProfile { profile ->
            if (index !in profile.hl.indices) return@updateActiveProfile profile
            profile.copy(hl = profile.hl.filterIndexed { i, _ -> i != index })
        }
        refreshStore()
    }

    fun moveHighlightEntry(from: Int, to: Int) {
        repository.updateActiveProfile { profile ->
            if (from !in profile.hl.indices || to !in profile.hl.indices || from == to) {
                return@updateActiveProfile profile
            }
            val list = profile.hl.toMutableList()
            val item = list.removeAt(from)
            list.add(to, item)
            profile.copy(hl = list)
        }
        refreshStore()
    }

    // Colors

    fun setColor(key: ReplaceColorKey, hex: String) {
        if (!HEX_REGEX.matches(hex)) return
        repository.updateColors { colors ->
            when (key) {
                ReplaceColorKey.CHAR -> colors.copy(char = hex)
                ReplaceColorKey.SKILL -> colors.copy(skill = hex)
                ReplaceColorKey.PLACE -> colors.copy(place = hex)
                ReplaceColorKey.QUOTE -> colors.copy(quote = hex)
                ReplaceColorKey.BRACKET -> colors.copy(bracket = hex)
                ReplaceColorKey.FIND_ACTIVE -> colors.copy(findActive = hex)
                ReplaceColorKey.REPLACE_TEXT -> colors.copy(replaceText = hex)
            }
        }
        refreshColors()
    }

    // JSON import/export

    fun replaceStore(newStore: ReplaceStore) {
        repository.setStore(newStore)
        refreshStore()
    }

    fun replaceColors(newColors: ReplaceColors) {
        repository.setColors(newColors)
        refreshColors()
    }

    fun activeProfile(): ReplaceProfile = repository.getStore().activeProfile()

    companion object {
        private val HEX_REGEX = Regex("^#[0-9a-fA-F]{6}$")
    }
}

enum class ReplaceColorKey(val label: String) {
    CHAR("Character"),
    SKILL("Skill"),
    PLACE("Place"),
    QUOTE("Quote"),
    BRACKET("Bracket"),
    REPLACE_TEXT("Replace"),
    FIND_ACTIVE("Find active"),
}
