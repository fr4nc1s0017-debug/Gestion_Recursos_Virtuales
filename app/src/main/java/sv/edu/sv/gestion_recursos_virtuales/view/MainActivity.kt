package sv.edu.udb.aprendeapp.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import sv.edu.udb.aprendeapp.R
import sv.edu.udb.aprendeapp.controller.ResourceController
import sv.edu.udb.aprendeapp.controller.ValidationException
import sv.edu.udb.aprendeapp.model.Recurso
import sv.edu.udb.aprendeapp.storage.FavoritesStore
import sv.edu.udb.aprendeapp.storage.SessionStore

class MainActivity : AppCompatActivity() {
    private val controller = ResourceController()
    private lateinit var session: SessionStore
    private lateinit var favorites: FavoritesStore
    private lateinit var adapter: ResourceAdapter
    private var resources: List<Recurso> = emptyList()
    private var favoritesOnly = false
    private lateinit var search: TextInputEditText
    private lateinit var filter: Spinner
    private lateinit var sort: Spinner
    private lateinit var empty: TextView
    private lateinit var progress: View
    private val types = listOf(null, "libro", "video", "artículo", "tutorial")
    private val launchEditor = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        loadResources()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionStore(this)
        favorites = FavoritesStore(this)
        if (session.id() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.greetingText).text = getString(R.string.greeting, session.nombre())
        search = findViewById(R.id.searchInput)
        filter = findViewById(R.id.filterSpinner)
        sort = findViewById(R.id.sortSpinner)
        empty = findViewById(R.id.emptyText)
        progress = findViewById(R.id.mainProgress)

        adapter = ResourceAdapter(
            isTeacher = session.isDocente(),
            isFavorite = { id -> favorites.contains(session.id().orEmpty(), id) },
            average = controller::promedio,
            openUrl = ::openUrl,
            toggleFavorite = ::toggleFavorite,
            rate = ::rateResource,
            edit = ::editResource,
            delete = ::confirmDelete
        )
        findViewById<RecyclerView>(R.id.resourcesList).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        setupSpinner(filter, R.array.filter_labels)
        setupSpinner(sort, R.array.sort_labels)
        findViewById<View>(R.id.addButton).apply {
            visibility = if (session.isDocente()) View.VISIBLE else View.GONE
            setOnClickListener { launchEditor.launch(Intent(this@MainActivity, EditorActivity::class.java)) }
        }
        findViewById<View>(R.id.refreshButton).setOnClickListener { loadResources() }
        val favBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.favoritesButton)
        favBtn.setOnClickListener {
            favoritesOnly = !favoritesOnly
            favBtn.setText(if (favoritesOnly) R.string.show_all else R.string.favorites_only)
            render()
        }
        findViewById<View>(R.id.logoutButton).setOnClickListener {
            session.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = render()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        loadResources()
    }

    private fun setupSpinner(spinner: Spinner, options: Int) {
        ArrayAdapter.createFromResource(this, options, android.R.layout.simple_spinner_item).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = it
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = render()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun render() {
        if (!::adapter.isInitialized) return
        val currentFavorites = if (favoritesOnly) favorites.ids(session.id().orEmpty()) else null
        val list = controller.filtrar(resources, search.text.toString(),
            types.getOrNull(filter.selectedItemPosition), sort.selectedItemPosition == 1, currentFavorites)
        adapter.submit(list)
        empty.visibility = if (list.isEmpty() && progress.visibility != View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun loadResources() {
        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            empty.visibility = View.GONE
            try {
                resources = controller.listar()
            } catch (_: Exception) {
                Toast.makeText(this@MainActivity, R.string.error_red, Toast.LENGTH_LONG).show()
            } finally {
                progress.visibility = View.GONE
                render()
            }
        }
    }

    private fun toggleFavorite(recurso: Recurso) {
        val id = recurso.id ?: return
        val added = favorites.toggle(session.id().orEmpty(), id)
        Toast.makeText(this, if (added) R.string.favorite_added else R.string.favorite_removed, Toast.LENGTH_SHORT).show()
        render()
    }

    private fun openUrl(recurso: Recurso) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recurso.enlace)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_open_url, Toast.LENGTH_LONG).show()
        }
    }

    private fun editResource(recurso: Recurso) {
        if (!session.isDocente()) return
        launchEditor.launch(Intent(this, EditorActivity::class.java).putExtra(EditorActivity.EXTRA_ID, recurso.id))
    }

    private fun confirmDelete(recurso: Recurso) {
        if (!session.isDocente()) return
        val id = recurso.id ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(getString(R.string.confirm_delete_text, recurso.titulo))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_resource) { _, _ ->
                lifecycleScope.launch {
                    progress.visibility = View.VISIBLE
                    try {
                        controller.eliminar(id)
                        Toast.makeText(this@MainActivity, R.string.deleted_resource, Toast.LENGTH_SHORT).show()
                        loadResources()
                    } catch (_: Exception) {
                        Toast.makeText(this@MainActivity, R.string.error_red, Toast.LENGTH_LONG).show()
                    } finally { progress.visibility = View.GONE }
                }
            }.show()
    }

    private fun rateResource(recurso: Recurso) {
        if (session.isDocente()) return
        val id = recurso.id ?: return
        val ratingBar = RatingBar(this).apply {
            numStars = 5
            stepSize = 1f
            rating = 5f
            setPadding(36, 20, 36, 20)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.rating_title)
            .setView(ratingBar)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                lifecycleScope.launch {
                    progress.visibility = View.VISIBLE
                    try {
                        controller.calificar(id, session.id().orEmpty(), ratingBar.rating.toInt())
                        Toast.makeText(this@MainActivity, R.string.rating_saved, Toast.LENGTH_SHORT).show()
                        loadResources()
                    } catch (e: ValidationException) {
                        Toast.makeText(this@MainActivity, e.resourceId, Toast.LENGTH_LONG).show()
                    } catch (_: Exception) {
                        Toast.makeText(this@MainActivity, R.string.error_red, Toast.LENGTH_LONG).show()
                    } finally { progress.visibility = View.GONE }
                }
            }.show()
    }
}
