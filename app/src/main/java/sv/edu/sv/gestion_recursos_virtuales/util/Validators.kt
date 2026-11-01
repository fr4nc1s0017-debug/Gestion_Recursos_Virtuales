package sv.edu.udb.aprendeapp.util

import java.net.URI
import sv.edu.sv.gestion_recursos_virtuales.R

/** Devolver IDs de strings permite centralizar TODOS los mensajes de validación. */
object Validators {
    private val emailRegex = Regex("^[A-Z0-9a-z._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    private val types = setOf("libro", "video", "artículo", "tutorial")

    fun emailError(email: String): Int? = when {
        email.isBlank() -> R.string.error_email_obligatorio
        !emailRegex.matches(email.trim()) -> R.string.error_email_invalido
        else -> null
    }

    fun passwordError(password: String): Int? = when {
        password.length < 12 -> R.string.error_password_longitud
        password.none { it.isUpperCase() } -> R.string.error_password_mayuscula
        password.none { it.isLowerCase() } -> R.string.error_password_minuscula
        password.none { it.isDigit() } -> R.string.error_password_numero
        password.none { it in "!@#\$%^&*" } -> R.string.error_password_simbolo
        else -> null
    }

    fun requiredError(value: String): Int? =
        if (value.isBlank()) R.string.error_campo_obligatorio else null

    fun urlError(value: String): Int? {
        if (value.isBlank()) return R.string.error_campo_obligatorio
        return try {
            val uri = URI(value.trim())
            if (uri.scheme?.lowercase() !in listOf("https", "http") || uri.host.isNullOrBlank())
                R.string.error_url_invalida else null
        } catch (_: Exception) { R.string.error_url_invalida }
    }

    fun imageUrlError(value: String): Int? =
        if (value.isBlank()) null else urlError(value)

    fun resourceError(titulo: String, descripcion: String, tipo: String, enlace: String, imagen: String): Int? = when {
        titulo.isBlank() || descripcion.isBlank() -> R.string.error_campo_obligatorio
        titulo.trim().length < 3 -> R.string.error_titulo_corto
        descripcion.trim().length < 10 -> R.string.error_descripcion_corta
        tipo !in types -> R.string.error_tipo_invalido
        urlError(enlace) != null -> urlError(enlace)
        imageUrlError(imagen) != null -> imageUrlError(imagen)
        else -> null
    }
}
