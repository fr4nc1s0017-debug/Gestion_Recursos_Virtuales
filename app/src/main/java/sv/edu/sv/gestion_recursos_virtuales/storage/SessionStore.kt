package sv.edu.udb.aprendeapp.storage

import android.content.Context
import sv.edu.udb.aprendeapp.model.Usuario

class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun save(usuario: Usuario) {
        preferences.edit()
            .putString("id", usuario.id)
            .putString("nombre", usuario.nombre)
            .putString("rol", usuario.rol)
            .apply()
    }

    fun id(): String? = preferences.getString("id", null)
    fun nombre(): String = preferences.getString("nombre", "").orEmpty()
    fun isDocente(): Boolean = preferences.getString("rol", "") == "docente"
    fun clear() = preferences.edit().clear().apply()
}
