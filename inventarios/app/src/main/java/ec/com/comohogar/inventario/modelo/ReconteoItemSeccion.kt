package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ReconteoItemSeccion(

    @SerializedName("id")
    val id: Long,
    @SerializedName("binId")
    var binId: Long,
    @SerializedName("itmId")
    var itmId: Long,
    @SerializedName("stock")
    var stock: Long,
    @SerializedName("cantidad")
    var cantidad: Long,
    @SerializedName("codigoI")
    var codigoI: String,
    @SerializedName("descripcion")
    var descripcion: String,
    @SerializedName("barras")
    var barras: String,
    @SerializedName("seccion")
    var seccion: String,
    @SerializedName("seccionId")
    val seccionId: Long,
    @SerializedName("ubicacionId")
    val ubicacionId: Long,
    @SerializedName("costoLanded")
    val costoLanded: BigDecimal,
    @SerializedName("movId")
    val movId: Long,
    @SerializedName("esPareto")
    var esPareto: String,
    @SerializedName("adjId")
    val adjId: Long,
    @SerializedName("ubicacion")
    var ubicacion: String,
    @SerializedName("costoTotal")
    val costoTotal: BigDecimal,
    @SerializedName("pvpNafConIva")
    val pvpNafConIva: BigDecimal,
    @SerializedName("pvpNafSinIva")
    val pvpNafSinIva: BigDecimal,
    @SerializedName("cobraIva")
    val cobraIva: Boolean,
    @SerializedName("lugId")
    val lugId: Long
)
