package eu.kanade.tachiyomi.data.replace

import kotlinx.serialization.json.Json
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

class ReplacePreferences(
    preferenceStore: PreferenceStore,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val store: Preference<ReplaceStore> = preferenceStore.getObjectFromString(
        key = "nekori_replace_store_v1",
        defaultValue = ReplaceStore.empty(),
        serializer = { json.encodeToString(ReplaceStore.serializer(), it) },
        deserializer = {
            runCatching { json.decodeFromString(ReplaceStore.serializer(), it) }
                .getOrDefault(ReplaceStore.empty())
        },
    )

    val colors: Preference<ReplaceColors> = preferenceStore.getObjectFromString(
        key = "nekori_replace_colors_v1",
        defaultValue = ReplaceColors.default(),
        serializer = { json.encodeToString(ReplaceColors.serializer(), it) },
        deserializer = {
            runCatching { json.decodeFromString(ReplaceColors.serializer(), it) }
                .getOrDefault(ReplaceColors.default())
        },
    )
}
