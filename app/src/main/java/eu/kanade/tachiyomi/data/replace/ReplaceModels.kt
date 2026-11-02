package eu.kanade.tachiyomi.data.replace

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReplaceHighlightType(val wire: String) {
    @SerialName("char")
    CHAR("char"),

    @SerialName("skill")
    SKILL("skill"),

    @SerialName("place")
    PLACE("place"),
    ;

    companion object {
        fun fromWire(value: String): ReplaceHighlightType =
            entries.firstOrNull { it.wire == value } ?: CHAR
    }
}

@Serializable
data class ReplaceFindEntry(
    val find: String = "",
    val replace: String = "",
    val enabled: Boolean = true,
)

@Serializable
data class ReplaceHighlightEntry(
    val text: String = "",
    val type: ReplaceHighlightType = ReplaceHighlightType.CHAR,
    val enabled: Boolean = true,
)

@Serializable
data class ReplaceProfile(
    val fr: List<ReplaceFindEntry> = emptyList(),
    val hl: List<ReplaceHighlightEntry> = emptyList(),
)

@Serializable
data class ReplaceStore(
    val active: String = DEFAULT_PROFILE_NAME,
    val list: Map<String, ReplaceProfile> = mapOf(DEFAULT_PROFILE_NAME to ReplaceProfile()),
) {
    fun activeProfile(): ReplaceProfile = list[active] ?: ReplaceProfile()

    fun withProfile(name: String, profile: ReplaceProfile): ReplaceStore =
        copy(list = list + (name to profile))

    fun renameProfile(from: String, to: String): ReplaceStore {
        if (from == to || !list.containsKey(from) || list.containsKey(to)) return this
        val newList = list.toMutableMap()
        val value = newList.remove(from) ?: return this
        newList[to] = value
        return copy(list = newList, active = if (active == from) to else active)
    }

    fun deleteProfile(name: String): ReplaceStore {
        if (list.size <= 1 || !list.containsKey(name)) return this
        val newList = list - name
        val newActive = if (active == name) newList.keys.first() else active
        return copy(list = newList, active = newActive)
    }

    companion object {
        const val DEFAULT_PROFILE_NAME = "default"

        fun empty(): ReplaceStore = ReplaceStore()
    }
}

@Serializable
data class ReplaceColors(
    val char: String = "#FB7185",
    val skill: String = "#E879F9",
    val place: String = "#5EEAD4",
    val quote: String = "#FDBA74",
    val bracket: String = "#818CF8",
    val findActive: String = "#ef5350",
    val replaceText: String = "#38BDF8",
) {
    companion object {
        fun default(): ReplaceColors = ReplaceColors()
    }
}
