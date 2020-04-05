package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class Inventario(

        @SerializedName("binId")
        var binId: kotlin.Long,
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
