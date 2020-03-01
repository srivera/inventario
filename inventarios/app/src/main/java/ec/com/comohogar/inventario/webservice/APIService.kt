package ec.com.comohogar.inventario.webservice

import ec.com.comohogar.inventario.modelo.UsuarioResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url

interface APIService {
    @GET
    fun consultarUsuario(@Url url:String): Call<UsuarioResponse>

    @Headers("usuario: administrador", "password: Asakus$2019","imei: d1e52ececbdba4d3")
    @GET("seguridad/login/")
    fun login(): Call<UsuarioResponse>
}