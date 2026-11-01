package sv.edu.udb.aprendeapp.controller

import sv.edu.sv.gestion_recursos_virtuales.R
import sv.edu.udb.aprendeapp.model.Usuario
import sv.edu.udb.aprendeapp.network.ApiClient
import sv.edu.udb.aprendeapp.util.PasswordHasher
import sv.edu.udb.aprendeapp.util.Validators

class ValidationException(val resourceId: Int) : IllegalArgumentException()

class AuthController {
    suspend fun login(email: String, password: String): Usuario {
        Validators.emailError(email)?.let { throw ValidationException(it) }
        if (password.isBlank()) throw ValidationException(R.string.error_password_requerida)
        val user = ApiClient.service.listarUsuarios()
            .firstOrNull { it.email.equals(email.trim(), ignoreCase = true) }
            ?: throw ValidationException(R.string.error_credenciales)
        if (!PasswordHasher.matches(password, user.salt, user.passwordHash))
            throw ValidationException(R.string.error_credenciales)
        return user
    }

    suspend fun registrar(nombre: String, email: String, password: String,
                          confirmacion: String, rol: String): Usuario {
        if (nombre.isBlank()) throw ValidationException(R.string.error_nombre_obligatorio)
        Validators.emailError(email)?.let { throw ValidationException(it) }
        Validators.passwordError(password)?.let { throw ValidationException(it) }
        if (password != confirmacion) throw ValidationException(R.string.error_password_no_coincide)
        if (rol !in listOf("estudiante", "docente")) throw ValidationException(R.string.error_rol_invalido)
        val users = ApiClient.service.listarUsuarios()
        if (users.any { it.email.equals(email.trim(), ignoreCase = true) })
            throw ValidationException(R.string.error_email_duplicado)
        val (salt, hash) = PasswordHasher.create(password)
        return ApiClient.service.crearUsuario(
            Usuario(nombre = nombre.trim(), email = email.trim().lowercase(),
                passwordHash = hash, salt = salt, rol = rol)
        )
    }
}
