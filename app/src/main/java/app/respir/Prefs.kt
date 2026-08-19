package app.respir

import android.content.Context

class Prefs(context: Context) {
    private val p = context.getSharedPreferences("respir", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = p.getBoolean("enabled", false)
        set(v) { p.edit().putBoolean("enabled", v).apply() }

    var intervalMinutes: Int
        get() = p.getInt("interval", 60)
        set(v) { p.edit().putInt("interval", v.coerceIn(5, 180)).apply() }

    var startedAt: Long
        get() = p.getLong("startedAt", 0L)
        set(v) { p.edit().putLong("startedAt", v).apply() }

    var lastCigaretteAt: Long
        get() = p.getLong("lastCig", 0L)
        set(v) { p.edit().putLong("lastCig", v).apply() }

    var resisted: Int
        get() = p.getInt("resisted", 0)
        set(v) { p.edit().putInt("resisted", v).apply() }

    var slips: Int
        get() = p.getInt("slips", 0)
        set(v) { p.edit().putInt("slips", v).apply() }

    fun ensureClock() {
        if (startedAt == 0L) {
            val now = System.currentTimeMillis()
            startedAt = now
            lastCigaretteAt = now
        }
    }
}
