package sv.edu.udb.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import sv.edu.sv.gestion_recursos_virtuales.R
import sv.edu.udb.aprendeapp.controller.AuthController
import sv.edu.udb.aprendeapp.controller.ValidationException

class RegisterActivity : AppCompatActivity() {
    private val controller = AuthController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val roles = findViewById<Spinner>(R.id.roleSpinner)
        ArrayAdapter.createFromResource(this, R.array.role_labels,
            android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            roles.adapter = it
        }
        findViewById<View>(R.id.backButton).setOnClickListener { finish() }
        val create = findViewById<View>(R.id.createButton)
        val progress = findViewById<View>(R.id.registerProgress)
        create.setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.nameInput).text.toString()
            val email = findViewById<TextInputEditText>(R.id.emailInput).text.toString()
            val password = findViewById<TextInputEditText>(R.id.passwordInput).text.toString()
            val confirmation = findViewById<TextInputEditText>(R.id.confirmInput).text.toString()
            val role = if (roles.selectedItemPosition == 0) "estudiante" else "docente"
            lifecycleScope.launch {
                create.isEnabled = false
                progress.visibility = View.VISIBLE
                try {
                    controller.registrar(name, email, password, confirmation, role)
                    Toast.makeText(this@RegisterActivity, R.string.saved_registration, Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: ValidationException) {
                    Toast.makeText(this@RegisterActivity, e.resourceId, Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(this@RegisterActivity, R.string.error_red, Toast.LENGTH_LONG).show()
                } finally {
                    create.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }
}
