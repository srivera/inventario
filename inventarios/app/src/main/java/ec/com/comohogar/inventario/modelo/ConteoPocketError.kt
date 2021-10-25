package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class ConteoPocketError(

    @SerializedName("pocId")
    val pocId: Long,
    @SerializedName("usuId")
    val usuId: Long,
    @SerializedName("pocZona")
    var pocZona: String,
    @SerializedName("pocBarra")
    var pocBarra: String,
    @SerializedName("cinId")
    var cinId: Long,
    @SerializedName("pocCantidad")
    val pocCantidad: Int,
    @SerializedName("binId")
    var binId: Long,
    @SerializedName("pocEstadoSync")
    val pocEstadoSync: Int,
    @SerializedName("pocObservacion")
    var pocObservacion: String,
    @SerializedName("corregido")
    var corregido: String
)
