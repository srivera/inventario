package ec.com.comohogar.inventario.webservice

import ec.com.comohogar.inventario.modelo.*
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
//    @Headers("usuario: administrador",
//        "password: Asakus$2019",
//        "imei: d1e52ececbdba4d3")
//    @GET("erp-rest/seguridad/login")
//    fun login(): Call<Usuario>

//    @GET("erp-movil/inv/consultarInventarioLugar/{idLocal}")
//    fun consultarInventarioLugar(@Path(value="idLocal") idLocal: Long?): Call<List<Inventario>>
//
//    @GET("erp-movil/inv/binTipoInventario/{idInventario}")
//    fun binTipoInventario(@Path(value="idInventario") idInventario: Int?): Call<Int>
//
//    @GET("erp-movil/inv/consultarLocales")
//    fun consultarLocales(): Call<List<Lugar>>
//
//    @GET("erp-movil/inv/consultarAsignacionUsuario/{empCodigo}/{nombrePocket}")
//    fun consultarAsignacionUsuario(@Path(value="empCodigo") empCodigo: Long,  @Path(value="nombrePocket")  nombrePocket: String): Call<List<AsignacionUsuario>>
//
//    @GET("erp-movil/inv/consultarConteo/{idConteo}/{idUsuario}/{barras}")
//    fun consultarConteo(@Path(value="idConteo") idConteo: Long,  @Path(value="idUsuario")  idUsuario: Long, @Path(value="barras")  barras: String): Call<Int>
//
//    @GET("erp-movil/inv/consultarErrores/{idUsuario}/{idInventario}")
//    fun consultarErrores(@Path(value="idUsuario") idUsuario: Long,  @Path(value="idInventario") idInventario: Long, @Query(value="numeroConteo")  idConteo: Long): Call<List<ConteoPocketError>>
//
//    @GET("erp-movil/inv/consultarHistorico/{idUsuario}/{idConteo}/{idInventario}")
//    fun consultarHistorico(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Int,
//                           @Path(value="idInventario") idInventario: Long,
//                           @Query(value="barras")  barras: String, @Query(value="zona")  zona: String): Call<List<ConteoPocketHistorico>>
//
//    @GET("erp-movil/inv/consultarNumeroConteo/{idInventario}")
//    fun consultarNumeroConteo(@Path(value="idInventario") idInventario: Long?): Call<List<Conteo>>
//
//    @GET("erp-movil/inv/consultarReconteoUsuario/{idInventario}/{numConteo}/{idUsuario}")
//    fun consultarReconteoUsuario(@Path(value="idInventario") idInventario: Long, @Path(value="numConteo") numConteo: Long,
//                                 @Path(value="idUsuario") idUsuario: Long): Call<List<ReconteoLocal>>
//
//    @GET("erp-movil/inv/consultarItem/{barras}")
//    fun consultarItem(@Path(value="barras") barras: String): Call<Item>
//
//    @GET("erp-movil/inv/consultarUsuario/{codigoEmpleado}")
//    fun consultarUsuario(@Path(value="codigoEmpleado") codigoEmpleado: Long): Call<Empleado>
//
//    @GET("erp-movil/inv/consultarZona/{codigo}")
//    fun consultarZona(@Path(value="codigo") codigo: String): Call<EstructuraBodega>
//
//    @GET("erp-movil/inv/ingresarConteo/{idUsuario}/{numeroConteo}/{barra}/{cantidad}/{fechaConteo}")
//    fun ingresarConteo(@Path(value="idUsuario") idUsuario: Long?,  @Path(value="numeroConteo")  numeroConteo: Long?,
//                       @Path(value="barra")  barra: String, @Path(value="cantidad")  cantidad: Int,
//                       @Path(value="fechaConteo")  fechaConteo: Long?, @Query(value="zona")  zona: String  ): Call<Long>
//
//    @GET("erp-movil/inv/ingresarConteoUsuario/{idUsuario}/{idConteo}/{zona}/{barra}/{cantidad}/{idInventario}/{numeroConteo}")
//    fun ingresarConteoUsuario(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
//                              @Path(value="zona")  zona: String, @Path(value="barra")  barra: String,
//                              @Path(value="cantidad")  cantidad: Long, @Path(value="idInventario") idInventario: Long,
//                              @Path(value="numeroConteo")  numeroConteo: Int): Call<Long>
//
//    @GET("erp-movil/inv/insertarConteoUsuarioRuta/{idUsuario}/{idConteo}/{zona}/{barra}/{cantidad}/{idInventario}/{idRuta}/{fechaConteo}")
//    fun insertarConteoUsuarioRuta(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
//                              @Path(value="zona")  zona: String, @Path(value="barra")  barra: String,
//                              @Path(value="cantidad")  cantidad: Long, @Path(value="idInventario") idInventario: Long,
//                              @Path(value="idRuta")  idRuta: Int, @Path(value="fechaConteo")  fechaConteo: Long?  ): Call<Long>
//
//    @GET("erp-movil/inv/ingresarUsuario/{idUsuario}/{idConteo}/{nombrePocket}")
//    fun ingresarUsuario(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
//                                  @Path(value="nombrePocket")  nombrePocket: String): Call<Long>
//
//    @GET("erp-movil/inv/cerrarUsuarioConteo/{idUsuario}/{idInventario}")
//    fun cerrarUsuarioConteo(@Path(value="idUsuario") idUsuario: Long, @Path(value="idInventario")  idInventario: Long): Call<Long>
//
//    @GET("erp-movil/inv/actualizarListaReconteo/{idUsuario}/{idInventario}/{numeroReconteo}/{cantidad}")
//    fun actualizarListaReconteo(@Path(value="idUsuario") idUsuario: Long, @Path(value="idInventario")  idInventario: Long,
//                                @Path(value="numeroReconteo")  numeroReconteo: Long, @Path(value="cantidad") cantidad: Int): Call<Long>
//
//
//    @GET("erp-movil/inv/consultarRutaUsuario/{idInventario}/{numConteo}/{idUsuario}")
//    fun consultarRutaUsuario(@Path(value="idInventario")  idInventario: Long, @Path(value="numConteo")  numConteo: Long,
//                             @Path(value="idUsuario") idUsuario: Long): Call<List<ReconteoBodega>>
//
//    @GET("erp-movil/inv/consultarLugarPorInventario/{idInventario}")
//    fun consultarLugarPorInventario(@Path(value="idInventario")  idInventario: Long): Call<Lugar>


    @GET("erp-movil/inv/consultarInventarioLugar/{idLocal}")
    fun consultarInventarioLugar(@Path(value="idLocal") idLocal: Long?): Call<List<Inventario>>

    @GET("erp-movil/inv/binTipoInventario/{idInventario}")
    fun binTipoInventario(@Path(value="idInventario") idInventario: Int?): Call<Int>

    @GET("erp-movil/inv/consultarLocales")
    fun consultarLocales(): Call<List<Lugar>>

    @GET("erp-movil/inv/consultarAsignacionUsuario/{empCodigo}/{nombrePocket}")
    fun consultarAsignacionUsuario(@Path(value="empCodigo") empCodigo: Long,  @Path(value="nombrePocket")  nombrePocket: String): Call<List<AsignacionUsuario>>

    @GET("erp-movil/inv/consultarConteo/{idConteo}/{idUsuario}/{barras}")
    fun consultarConteo(@Path(value="idConteo") idConteo: Long,  @Path(value="idUsuario")  idUsuario: Long, @Path(value="barras")  barras: String): Call<Int>

    @GET("erp-movil/inv/consultarErrores/{idUsuario}/{idInventario}")
    fun consultarErrores(@Path(value="idUsuario") idUsuario: Long,  @Path(value="idInventario") idInventario: Long, @Query(value="numeroConteo")  idConteo: Long): Call<List<ConteoPocketError>>

    @GET("erp-movil/inv/consultarHistorico/{idUsuario}/{idConteo}/{idInventario}")
    fun consultarHistorico(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Int,
                           @Path(value="idInventario") idInventario: Long,
                           @Query(value="barras")  barras: String, @Query(value="zona")  zona: String): Call<List<ConteoPocketHistorico>>

    @GET("erp-movil/inv/consultarNumeroConteo/{idInventario}")
    fun consultarNumeroConteo(@Path(value="idInventario") idInventario: Long?): Call<List<Conteo>>

    @GET("erp-movil/inv/consultarReconteoUsuario/{idInventario}/{numConteo}/{idUsuario}")
    fun consultarReconteoUsuario(@Path(value="idInventario") idInventario: Long, @Path(value="numConteo") numConteo: Long,
                                 @Path(value="idUsuario") idUsuario: Long): Call<List<ReconteoLocal>>

    @GET("erp-movil/inv/consultarReconteoUsuario/{idInventario}/{numConteo}/{idUsuario}")
    fun consultarReconteoUsuarioV2(@Path(value="idInventario") idInventario: Long, @Path(value="numConteo") numConteo: Long,
                                 @Path(value="idUsuario") idUsuario: Long): Call<List<ReconteoBodega>>

    @GET("erp-movil/inv/consultarItem/{barras}")
    fun consultarItem(@Path(value="barras") barras: String): Call<Item>

    @GET("erp-movil/inv/consultarUsuario/{codigoEmpleado}")
    fun consultarUsuario(@Path(value="codigoEmpleado") codigoEmpleado: Long): Call<Empleado>

    @GET("erp-movil/inv/consultarZona/{codigo}")
    fun consultarZona(@Path(value="codigo") codigo: String): Call<EstructuraBodega>

    @GET("erp-movil/inv/ingresarConteo/{idUsuario}/{numeroConteo}/{barra}/{cantidad}/{fechaConteo}")
    fun ingresarConteo(@Path(value="idUsuario") idUsuario: Long?,  @Path(value="numeroConteo")  numeroConteo: Long?,
                       @Path(value="barra")  barra: String, @Path(value="cantidad")  cantidad: Int,
                       @Path(value="fechaConteo")  fechaConteo: Long?, @Query(value="zona")  zona: String  ): Call<Long>

    @GET("erp-movil/inv/ingresarConteoUsuario/{idUsuario}/{idConteo}/{zona}/{barra}/{cantidad}/{idInventario}/{numeroConteo}")
    fun ingresarConteoUsuario(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
                              @Path(value="zona")  zona: String, @Path(value="barra")  barra: String,
                              @Path(value="cantidad")  cantidad: Long, @Path(value="idInventario") idInventario: Long,
                              @Path(value="numeroConteo")  numeroConteo: Int): Call<Long>

    @GET("erp-movil/inv/actualizarConteo/{idUsuario}/{numeroConteo}/{barra}/{cantidad}/{fechaConteo}/{idInventario}/{idConteo}/{pocId}/{zona}")
    fun actualizarConteo(@Path(value="idUsuario") idUsuario: Long, @Path(value="numeroConteo")  numeroConteo: Int,
                                @Path(value="barra")  barra: String, @Path(value="cantidad")  cantidad: Long,
                                @Path(value="fechaConteo")  fechaConteo: Long?, @Path(value="idInventario") idInventario: Long,
                                @Path(value="idConteo")  idConteo: Long, @Path(value="pocId")  pocId: Long,
                                @Path(value="zona")  zona: String): Call<Long>


    @GET("erp-movil/inv/insertarConteoUsuarioRuta/{idUsuario}/{idConteo}/{zona}/{barra}/{cantidad}/{idInventario}/{idRuta}/{fechaConteo}")
    fun insertarConteoUsuarioRuta(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
                                  @Path(value="zona")  zona: String, @Path(value="barra")  barra: String,
                                  @Path(value="cantidad")  cantidad: Long, @Path(value="idInventario") idInventario: Long,
                                  @Path(value="idRuta")  idRuta: Int, @Path(value="fechaConteo")  fechaConteo: Long?  ): Call<Long>

    @GET("erp-movil/inv/ingresarUsuario/{idUsuario}/{idConteo}/{nombrePocket}")
    fun ingresarUsuario(@Path(value="idUsuario") idUsuario: Long, @Path(value="idConteo")  idConteo: Long,
                        @Path(value="nombrePocket")  nombrePocket: String): Call<Long>

    @GET("erp-movil/inv/cerrarUsuarioConteo/{idUsuario}/{idInventario}")
    fun cerrarUsuarioConteo(@Path(value="idUsuario") idUsuario: Long, @Path(value="idInventario")  idInventario: Long): Call<Long>

    @GET("erp-movil/inv/actualizarListaReconteo/{idUsuario}/{idInventario}/{numeroReconteo}/{cantidad}")
    fun actualizarListaReconteo(@Path(value="idUsuario") idUsuario: Long, @Path(value="idInventario")  idInventario: Long,
                                @Path(value="numeroReconteo")  numeroReconteo: Long, @Path(value="cantidad") cantidad: Int): Call<Long>


    @GET("erp-movil/inv/consultarRutaUsuario/{idInventario}/{numConteo}/{idUsuario}")
    fun consultarRutaUsuario(@Path(value="idInventario")  idInventario: Long, @Path(value="numConteo")  numConteo: Long,
                             @Path(value="idUsuario") idUsuario: Long): Call<List<ReconteoBodega>>

    @GET("erp-movil/inv/consultarLugarPorInventario/{idInventario}")
    fun consultarLugarPorInventario(@Path(value="idInventario")  idInventario: Long): Call<Lugar>

    @GET("erp-movil/inv/consultarConteoUsuarioTotal/{idInventario}/{numeroConteo}")
    fun consultarConteoUsuarioTotal(@Path(value="idInventario")  idInventario: Long, @Path(value="numeroConteo")  numeroConteo: Long): Call<List<ReconteoAsignado>>


    @GET("erp-movil/inv/consultarConteoInventarioUsuario/{idUsuario}/{idInventario}/{numeroConteo}/{esReconteo}")
    fun consultarConteoInventarioUsuario(@Path(value="idUsuario") idUsuario: Long, @Path(value="idInventario")  idInventario: Long,
                                         @Path(value="numeroConteo")  numeroConteo: Long, @Path(value="esReconteo")  esReconteo: String): Call<List<ReconteoItemSeccion>>
}
