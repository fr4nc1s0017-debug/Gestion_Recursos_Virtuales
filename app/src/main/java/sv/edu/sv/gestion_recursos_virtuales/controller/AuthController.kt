package sv.edu.udb.aprendeapp.controller

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import sv.edu.sv.gestion_recursos_virtuales.R
import sv.edu.udb.aprendeapp.network.ApiClient
import sv.edu.udb.aprendeapp.model.Usuario
import sv.edu.udb.aprendeapp.util.Validators

class ValidationException(
    val resourceId: Int
) : IllegalArgumentException()

class AuthController {

    private val auth = FirebaseAuth.getInstance()

    suspend fun login(
        email: String,
        password: String
    ): Usuario {

        Validators.emailError(email)?.let {
            throw ValidationException(it)
        }

        if (password.isBlank()) {
            throw ValidationException(
                R.string.error_password_requerida
            )
        }

        val firebaseUser = auth
            .signInWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()
            .user
            ?: throw ValidationException(
                R.string.error_credenciales
            )

        return obtenerOCrearPerfil(firebaseUser)
    }

    suspend fun registrar(
        nombre: String,
        email: String,
        password: String,
        confirmacion: String
    ): Usuario {

        if (nombre.isBlank()) {
            throw ValidationException(
                R.string.error_nombre_obligatorio
            )
        }

        Validators.emailError(email)?.let {
            throw ValidationException(it)
        }

        Validators.passwordError(password)?.let {
            throw ValidationException(it)
        }

        if (password != confirmacion) {
            throw ValidationException(
                R.string.error_password_no_coincide
            )
        }

        val firebaseUser = auth
            .createUserWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()
            .user
            ?: throw ValidationException(
                R.string.error_credenciales
            )

        return obtenerOCrearPerfil(
            firebaseUser,
            nombre.trim()
        )
    }

    suspend fun loginGoogle(
        idToken: String
    ): Usuario {

        val credential =
            GoogleAuthProvider.getCredential(idToken, null)

        val firebaseUser = auth
            .signInWithCredential(credential)
            .await()
            .user
            ?: throw ValidationException(
                R.string.error_credenciales
            )

        return obtenerOCrearPerfil(firebaseUser)
    }

    private suspend fun obtenerOCrearPerfil(
        firebaseUser: FirebaseUser,
        nombreNuevo: String? = null
    ): Usuario {

        val usuarios = ApiClient.apiService
            .listarUsuarios()

        val existente = usuarios.firstOrNull {
            it.firebaseUid == firebaseUser.uid
        }

        if (existente != null) {
            return existente
        }

        val correo = firebaseUser.email.orEmpty()

        if (usuarios.any {
                it.email.equals(correo, ignoreCase = true)
            }) {
            throw ValidationException(
                R.string.error_perfil_conflicto
            )
        }

        val nombre = nombreNuevo
            ?.takeIf { it.isNotBlank() }
            ?: firebaseUser.displayName
                ?.takeIf { it.isNotBlank() }
            ?: correo.substringBefore("@")
                .takeIf { it.isNotBlank() }
            ?: throw ValidationException(
                R.string.error_nombre_obligatorio
            )

        val nuevoUsuario = Usuario(
            firebaseUid = firebaseUser.uid,
            nombre = nombre,
            email = correo,
            rol = "estudiante"
        )

        return ApiClient.apiService
            .crearUsuario(nuevoUsuario)
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }
}
