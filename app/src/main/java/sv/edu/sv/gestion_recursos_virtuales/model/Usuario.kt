package sv.edu.udb.aprendeapp.model

data class Usuario(
    val id: String = "",
    val firebaseUid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "estudiante"
)