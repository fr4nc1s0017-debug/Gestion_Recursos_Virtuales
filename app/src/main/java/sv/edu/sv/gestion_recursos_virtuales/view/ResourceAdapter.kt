package sv.edu.udb.aprendeapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import sv.edu.udb.aprendeapp.R
import sv.edu.udb.aprendeapp.model.Recurso

class ResourceAdapter(
    private val isTeacher: Boolean,
    private val isFavorite: (String) -> Boolean,
    private val average: (Recurso) -> Double,
    private val openUrl: (Recurso) -> Unit,
    private val toggleFavorite: (Recurso) -> Unit,
    private val rate: (Recurso) -> Unit,
    private val edit: (Recurso) -> Unit,
    private val delete: (Recurso) -> Unit
) : RecyclerView.Adapter<ResourceAdapter.ResourceHolder>() {

    private var items: List<Recurso> = emptyList()

    fun submit(items: List<Recurso>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resource, parent, false)
        return ResourceHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ResourceHolder, position: Int) {
        val resource = items[position]
        val context = holder.itemView.context
        holder.title.text = resource.titulo
        holder.description.text = resource.descripcion
        holder.type.text = context.getString(R.string.resource_type, resource.tipo)
        val avg = average(resource)
        holder.rating.text = if (avg <= 0.0) context.getString(R.string.rating_not_available)
                             else context.getString(R.string.rating_average, avg)
        Glide.with(holder.itemView)
            .load(resource.imagen.ifBlank { null })
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.image)

        holder.favorite.text = context.getString(
            if (isFavorite(resource.id.orEmpty())) R.string.favorite_remove else R.string.favorite_add
        )
        holder.link.setOnClickListener { openUrl(resource) }
        holder.favorite.setOnClickListener { toggleFavorite(resource) }
        holder.rate.visibility = if (isTeacher) View.GONE else View.VISIBLE
        holder.edit.visibility = if (isTeacher) View.VISIBLE else View.GONE
        holder.delete.visibility = if (isTeacher) View.VISIBLE else View.GONE
        holder.rate.setOnClickListener { rate(resource) }
        holder.edit.setOnClickListener { edit(resource) }
        holder.delete.setOnClickListener { delete(resource) }
    }

    class ResourceHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.resourceImage)
        val title: TextView = view.findViewById(R.id.titleText)
        val description: TextView = view.findViewById(R.id.descriptionText)
        val type: TextView = view.findViewById(R.id.typeText)
        val rating: TextView = view.findViewById(R.id.ratingText)
        val link: MaterialButton = view.findViewById(R.id.linkButton)
        val favorite: MaterialButton = view.findViewById(R.id.favoriteButton)
        val rate: MaterialButton = view.findViewById(R.id.rateButton)
        val edit: MaterialButton = view.findViewById(R.id.editButton)
        val delete: MaterialButton = view.findViewById(R.id.deleteButton)
    }
}
