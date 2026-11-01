package sv.edu.udb.aprendeapp.controller

import org.json.JSONObject
import sv.edu.sv.gestion_recursos_virtuales.R
import sv.edu.udb.aprendeapp.model.Recurso
import sv.edu.udb.aprendeapp.network.ApiClient
import sv.edu.udb.aprendeapp.util.Validators

class ResourceController {
    suspend fun listar(): List<Recurso> = ApiClient.service.listarRecursos()
    suspend fun obtener(id: String): Recurso = ApiClient.service.obtenerRecurso(id)

    suspend fun guardar(id: String?, titulo: String, descripcion: String,
                        tipo: String, enlace: String, imagen: String): Recurso {
        Validators.resourceError(titulo, descripcion, tipo, enlace, imagen)
            ?.let { throw ValidationException(it) }
        val original = if (id == null) null else obtener(id)
        val recurso = Recurso(id = id, titulo = titulo.trim(), descripcion = descripcion.trim(),
            tipo = tipo, enlace = enlace.trim(), imagen = imagen.trim(),
            calificaciones = original?.calificaciones ?: "{}")
        return if (id == null) ApiClient.service.crearRecurso(recurso)
               else ApiClient.service.actualizarRecurso(id, recurso)
    }

    suspend fun eliminar(id: String) { ApiClient.service.eliminarRecurso(id) }

    suspend fun calificar(id: String, usuarioId: String, estrellas: Int): Recurso {
        if (estrellas !in 1..5) throw ValidationException(R.string.error_rating_invalido)
        // Volver a consultar evita escribir una copia antigua de las calificaciones.
        val actual = obtener(id)
        val votos = try { JSONObject(actual.calificaciones) } catch (_: Exception) { JSONObject() }
        votos.put(usuarioId, estrellas)
        return ApiClient.service.actualizarRecurso(id, actual.copy(calificaciones = votos.toString()))
    }

    fun promedio(recurso: Recurso): Double {
        val votos = try { JSONObject(recurso.calificaciones) } catch (_: Exception) { JSONObject() }
        val numeros = mutableListOf<Int>()
        val keys = votos.keys()
        while (keys.hasNext()) {
            val n = votos.optInt(keys.next(), 0)
            if (n in 1..5) numeros.add(n)
        }
        return if (numeros.isEmpty()) 0.0 else numeros.average()
    }

    fun filtrar(recursos: List<Recurso>, busqueda: String, tipo: String?,
                ordenarPorTipo: Boolean, soloFavoritos: Set<String>?): List<Recurso> {
        val texto = busqueda.trim()
        return recursos.asSequence()
            .filter { r -> texto.isBlank() || r.id.orEmpty().contains(texto, true) ||
                r.titulo.contains(texto, true) || r.tipo.contains(texto, true) }
            .filter { r -> tipo == null || r.tipo == tipo }
            .filter { r -> soloFavoritos == null || r.id in soloFavoritos }
            .sortedWith(if (ordenarPorTipo) compareBy<Recurso> { it.tipo }.thenBy { it.titulo }
                        else compareBy(String.CASE_INSENSITIVE_ORDER) { it.titulo })
            .toList()
    }
}
