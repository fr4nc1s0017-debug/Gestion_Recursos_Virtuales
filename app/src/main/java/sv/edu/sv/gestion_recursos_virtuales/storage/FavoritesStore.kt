package sv.edu.udb.aprendeapp.storage

import android.content.Context

/** Favoritos aislados por usuario; persisten aunque se cierre la aplicación. */
class FavoritesStore(context: Context) {
    private val preferences = context.getSharedPreferences("favoritos", Context.MODE_PRIVATE)
    private fun key(userId: String) = "favoritos_$userId"

    fun ids(userId: String): Set<String> =
        preferences.getStringSet(key(userId), emptySet()).orEmpty().toSet()

    fun contains(userId: String, recursoId: String): Boolean = recursoId in ids(userId)

    fun toggle(userId: String, recursoId: String): Boolean {
        val current = ids(userId).toMutableSet()
        val inserted = if (recursoId in current) { current.remove(recursoId); false }
                       else { current.add(recursoId); true }
        preferences.edit().putStringSet(key(userId), current).apply()
        return inserted
    }
}
