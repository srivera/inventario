package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class Conteo(

        @SerializedName("cinId")
        var cinId: kotlin.Long,
        @SerializedName("binId")
        var binId: kotlin.Long,
        @SerializedName("cinObservacion")
        val cinObservacion: String,
        @SerializedName("usuId")
        val usuId: Long,
        @SerializedName("lugNombre")
        val lugNombre: String,
        @SerializedName("cinNumConteo")
        var cinNumConteo: kotlin.Int,
        @SerializedName("cinEstadoConteo")
        val cinEstadoConteo: Int,
        @SerializedName("cinTipoConteo")
        val cinTipoConteo: String)

