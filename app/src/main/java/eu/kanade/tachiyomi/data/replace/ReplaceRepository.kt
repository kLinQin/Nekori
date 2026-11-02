package eu.kanade.tachiyomi.data.replace

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

class ReplaceRepository(
    preferenceStore: PreferenceStore,
) {
    private val prefs = ReplacePreferences(preferenceStore)

    val storeFlow: Flow<ReplaceStore> = prefs.store.changes().map { it }
    val colorsFlow: Flow<ReplaceColors> = prefs.colors.changes().map { it }

    fun getStore(): ReplaceStore = prefs.store.get()
    fun getColors(): ReplaceColors = prefs.colors.get()

    fun setStore(store: ReplaceStore) = prefs.store.set(store)
    fun setColors(colors: ReplaceColors) = prefs.colors.set(colors)

    fun setActiveProfile(name: String) {
        val current = getStore()
        if (!current.list.containsKey(name) || current.active == name) return
        setStore(current.copy(active = name))
    }

    fun addProfile(name: String) {
        val current = getStore()
        if (name.isBlank() || current.list.containsKey(name)) return
        setStore(current.withProfile(name, ReplaceProfile()).copy(active = name))
    }

    fun renameProfile(from: String, to: String) {
        setStore(getStore().renameProfile(from, to))
    }

    fun deleteProfile(name: String) {
        setStore(getStore().deleteProfile(name))
    }

    fun updateActiveProfile(transform: (ReplaceProfile) -> ReplaceProfile) {
        val current = getStore()
        val profile = current.activeProfile()
        setStore(current.withProfile(current.active, transform(profile)))
    }

    fun updateColors(transform: (ReplaceColors) -> ReplaceColors) {
        setColors(transform(getColors()))
    }
}
