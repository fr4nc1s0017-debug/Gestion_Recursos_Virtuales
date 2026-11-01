package sv.edu.udb.aprendeapp.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import sv.edu.udb.aprendeapp.model.Recurso
import sv.edu.udb.aprendeapp.model.Usuario

interface ApiService {
    @GET("usuarios") suspend fun listarUsuarios(): List<Usuario>
    @POST("usuarios") suspend fun crearUsuario(@Body usuario: Usuario): Usuario

    @GET("recursos") suspend fun listarRecursos(): List<Recurso>
    @GET("recursos/{id}") suspend fun obtenerRecurso(@Path("id") id: String): Recurso
    @POST("recursos") suspend fun crearRecurso(@Body recurso: Recurso): Recurso
    @PUT("recursos/{id}") suspend fun actualizarRecurso(@Path("id") id: String, @Body recurso: Recurso): Recurso
    @DELETE("recursos/{id}") suspend fun eliminarRecurso(@Path("id") id: String): Recurso
}
