package sv.edu.udb.aprendeapp.model

data class Recurso(
    val id: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val enlace: String = "",
    val imagen: String = "",
    // JSON serializado { "idUsuario": calificacion }, almacenado en el backend.
    val calificaciones: String = "{}"
)
