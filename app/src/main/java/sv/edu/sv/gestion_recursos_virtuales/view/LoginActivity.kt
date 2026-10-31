package sv.edu.udb.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import sv.edu.udb.aprendeapp.R
import sv.edu.udb.aprendeapp.controller.AuthController
import sv.edu.udb.aprendeapp.controller.ValidationException
import sv.edu.udb.aprendeapp.storage.SessionStore

class LoginActivity : AppCompatActivity() {
    private val controller = AuthController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SessionStore(this).id() != null) {
            openMain()
            return
        }
        setContentView(R.layout.activity_login)

        val email = findViewById<TextInputEditText>(R.id.emailInput)
        val password = findViewById<TextInputEditText>(R.id.passwordInput)
        val progress = findViewById<View>(R.id.loginProgress)
        val login = findViewById<View>(R.id.loginButton)

        findViewById<View>(R.id.registerButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        login.setOnClickListener {
            lifecycleScope.launch {
                login.isEnabled = false
                progress.visibility = View.VISIBLE
                try {
                    val usuario = controller.login(email.text.toString(), password.text.toString())
                    SessionStore(this@LoginActivity).save(usuario)
                    openMain()
                } catch (e: ValidationException) {
                    Toast.makeText(this@LoginActivity, e.resourceId, Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(this@LoginActivity, R.string.error_red, Toast.LENGTH_LONG).show()
                } finally {
                    login.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
