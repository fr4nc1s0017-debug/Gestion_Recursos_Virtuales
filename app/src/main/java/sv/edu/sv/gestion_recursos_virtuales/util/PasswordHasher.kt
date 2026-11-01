package sv.edu.udb.aprendeapp.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PBKDF2 reduce la exposición de contraseñas, pero MockAPI NO es un servidor de autenticación seguro. */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_BITS = 256

    fun create(password: String): Pair<String, String> {
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        return salt to derive(password, saltBytes)
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean = try {
        val derived = derive(password, Base64.decode(salt, Base64.NO_WRAP))
        MessageDigest.isEqual(
            Base64.decode(expectedHash, Base64.NO_WRAP),
            Base64.decode(derived, Base64.NO_WRAP)
        )
    } catch (_: Exception) { false }

    private fun derive(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally { spec.clearPassword() }
    }
}
