package eu.kanade.tachiyomi.ui.reader.replace

/**
 * In-memory history for replace input in ReplaceQuickAddSheet.
 * Lost when app process dies. Max 30 items, newest first.
 */
internal object ReplaceHistory {
    private const val MAX = 30
    private val items = ArrayDeque<String>()

    fun snapshot(): List<String> = items.toList()

    fun push(value: String) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return
        items.remove(trimmed)
        items.addFirst(trimmed)
        while (items.size > MAX) items.removeLast()
    }
}
