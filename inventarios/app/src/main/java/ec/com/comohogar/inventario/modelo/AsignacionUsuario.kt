package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class AsignacionUsuario(

        @SerializedName("uinId")
        var uinId: kotlin.Long,
        @SerializedName("uinNombrePocket")
        val uinNombrePocket: String,
        @SerializedName("uinEstado")
        val uinEstado: Int,
        @SerializedName("uinObservacion")
        val uinObservacion: String,
        @SerializedName("cinId")
        var cinId: kotlin.Long,
        @SerializedName("binId")
        val binId: Long,
        @SerializedName("uinCantidadItem")
        val uinCantidadItem: Int,
        @SerializedName("uinValorTotal")
        val uinValorTotal: Double,
        @SerializedName("uinSecuencial")
        val uinSecuencial: String,
        @SerializedName("usuId")
        val usuId: Long,
        @SerializedName("empCodigo")
        val empCodigo: String,
        @SerializedName("tipoInventario")
        val tipoInventario: Int,
        @SerializedName("numeroConteo")
        val numeroConteo: Long)

