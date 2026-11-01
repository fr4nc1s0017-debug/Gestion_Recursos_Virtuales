package sv.edu.udb.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import sv.edu.sv.gestion_recursos_virtuales.R
import sv.edu.udb.aprendeapp.controller.ResourceController
import sv.edu.udb.aprendeapp.controller.ValidationException
import sv.edu.udb.aprendeapp.storage.SessionStore

class EditorActivity : AppCompatActivity() {
    companion object { const val EXTRA_ID = "recurso_id" }
    private val controller = ResourceController()
    private val types = listOf("libro", "video", "artículo", "tutorial")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionStore(this).isDocente()) {
            Toast.makeText(this, R.string.session_expired, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContentView(R.layout.activity_editor)
        val title = findViewById<TextInputEditText>(R.id.titleInput)
        val description = findViewById<TextInputEditText>(R.id.descriptionInput)
        val link = findViewById<TextInputEditText>(R.id.linkInput)
        val image = findViewById<TextInputEditText>(R.id.imageInput)
        val spinner = findViewById<Spinner>(R.id.typeSpinner)
        val save = findViewById<View>(R.id.saveButton)
        val progress = findViewById<View>(R.id.editorProgress)
        ArrayAdapter.createFromResource(this, R.array.type_labels,
            android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }
        val id = intent.getStringExtra(EXTRA_ID)
        if (id != null) {
            findViewById<TextView>(R.id.editorHeading).setText(R.string.edit_resource)
            save.isEnabled = false
            lifecycleScope.launch {
                progress.visibility = View.VISIBLE
                try {
                    val resource = controller.obtener(id)
                    title.setText(resource.titulo)
                    description.setText(resource.descripcion)
                    link.setText(resource.enlace)
                    image.setText(resource.imagen)
                    spinner.setSelection(types.indexOf(resource.tipo).coerceAtLeast(0))
                    save.isEnabled = true
                } catch (_: Exception) {
                    Toast.makeText(this@EditorActivity, R.string.error_red, Toast.LENGTH_LONG).show()
                    finish()
                } finally { progress.visibility = View.GONE }
            }
        }
        save.setOnClickListener {
            lifecycleScope.launch {
                save.isEnabled = false
                progress.visibility = View.VISIBLE
                try {
                    controller.guardar(id, title.text.toString(), description.text.toString(),
                        types[spinner.selectedItemPosition], link.text.toString(), image.text.toString())
                    Toast.makeText(this@EditorActivity, R.string.saved_resource, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } catch (e: ValidationException) {
                    Toast.makeText(this@EditorActivity, e.resourceId, Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(this@EditorActivity, R.string.error_red, Toast.LENGTH_LONG).show()
                } finally {
                    save.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }
}
