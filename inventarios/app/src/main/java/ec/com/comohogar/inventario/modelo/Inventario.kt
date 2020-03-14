package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


data class Inventario(


        @SerializedName("binId")
        var binId: kotlin.Long,
        @SerializedName("binFecha")
        var binFecha: Date,
        @SerializedName("binTipoInventario")
        val binTipoInventario: Int,
        @SerializedName("binTipoMuestra")
        val binTipoMuestra: Int,
        @SerializedName("binObservacion")
        val binObservacion: String,
        @SerializedName("binEstado")
        var binEstado: kotlin.Int,
        @SerializedName("binStatusWf")
        val binStatusWf: Int,
        @SerializedName("usuId")
        val usuId: Long)

