
package sv.edu.udb.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch

import sv.edu.sv.gestion_recursos_virtuales.R
import sv.edu.udb.aprendeapp.controller.AuthController
import sv.edu.udb.aprendeapp.controller.ValidationException

class RegisterActivity : AppCompatActivity() {

    private val controller = AuthController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        // Botón para regresar
        findViewById<View>(R.id.backButton)
            .setOnClickListener {
                finish()
            }

        // Elementos del formulario
        val nameInput =
            findViewById<TextInputEditText>(R.id.nameInput)

        val emailInput =
            findViewById<TextInputEditText>(R.id.emailInput)

        val passwordInput =
            findViewById<TextInputEditText>(R.id.passwordInput)

        val confirmInput =
            findViewById<TextInputEditText>(R.id.confirmInput)

        val createButton =
            findViewById<View>(R.id.createButton)

        val progress =
            findViewById<View>(R.id.registerProgress)

        createButton.setOnClickListener {

            val name = nameInput.text
                ?.toString()
                ?.trim()
                .orEmpty()

            val email = emailInput.text
                ?.toString()
                ?.trim()
                .orEmpty()

            val password = passwordInput.text
                ?.toString()
                .orEmpty()

            val confirmation = confirmInput.text
                ?.toString()
                .orEmpty()

            lifecycleScope.launch {

                createButton.isEnabled = false
                progress.visibility = View.VISIBLE

                try {
                    // Firebase Authentication
                    // El rol se asigna como estudiante
                    controller.registrar(
                        name,
                        email,
                        password,
                        confirmation
                    )

                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.saved_registration,
                        Toast.LENGTH_LONG
                    ).show()

                    finish()

                } catch (e: ValidationException) {

                    Toast.makeText(
                        this@RegisterActivity,
                        e.resourceId,
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: FirebaseAuthUserCollisionException) {

                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.error_email_duplicado,
                        Toast.LENGTH_LONG
                    ).show()

                } catch (e: Exception) {

                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.error_registro_fallido,
                        Toast.LENGTH_LONG
                    ).show()

                } finally {

                    createButton.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }
}
