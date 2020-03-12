package ec.com.comohogar.inventario.webservice

import ec.com.comohogar.inventario.modelo.Usuario
import ec.com.comohogar.inventario.modelo.UsuarioResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Url

interface APIService {
    @GET
    fun consultarUsuario(@Url url:String): Call<UsuarioResponse>

    @Headers("usuario: administrador",
        "password: Asakus$2019",
        "imei: d1e52ececbdba4d3")
    @GET("erp-rest/seguridad/login")
    fun login(): Call<Usuario>


    @GET("erp-movil/inv/ingresarConteoUsuario/{idUsuario}/{idConteo}/{zona}/{barra}/{cantidad}/{idInventario}/{numeroConteo}")
    fun ingresarConteoUsuario(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
                              @Path(value="zona")  zona: String, @Path(value="barra")  barra: String,
                              @Path(value="cantidad")  cantidad: Long, @Path(value="idInventario") idInventario: Long,
                              @Path(value="numeroConteo")  numeroConteo: Int): Call<Usuario>

    @GET("erp-movil/inv/ingresarConteo/{idUsuario}/{numeroConteo}/{zona}/{barra}/{cantidad}")
    fun ingresarConteo(@Path(value="idUsuario") idUsuario: Long,  @Path(value="numeroConteo")  numeroConteo: Long,
                              @Path(value="zona")  zona: String, @Path(value="barra")  barra: String,
                              @Path(value="cantidad")  cantidad: Int): Call<Long>

}