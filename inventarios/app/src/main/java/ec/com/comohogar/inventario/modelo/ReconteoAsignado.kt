package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.sql.Timestamp

data class ReconteoAsignado(

    @SerializedName("usuId")
    val usuId: Long,
    @SerializedName("binId")
    var binId: Long,
    @SerializedName("totalAsignado")
    var totalAsignado: Long,
    @SerializedName("totalContados")
    var totalContados: Long,
    @SerializedName("costoAsignado")
    val costoAsignado: BigDecimal,
    @SerializedName("costoContado")
    val costoContado: BigDecimal,
    @SerializedName("cinNumConteo")
    var cinNumConteo: Long,
    @SerializedName("nombre")
    var nombre: String,
    @SerializedName("apellido")
    var apellido: String,
    @SerializedName("codigo")
    val codigo: Long,
    @SerializedName("fechaMin")
    val fechaMin: Long,
    @SerializedName("fechaMax")
    val fechaMax: Long,
    @SerializedName("estado")
    val estado: Long,
    @SerializedName("unidades")
    val unidades: Long,
    @SerializedName("porcentajeAvance")
    var porcentajeAvance: Int

)
