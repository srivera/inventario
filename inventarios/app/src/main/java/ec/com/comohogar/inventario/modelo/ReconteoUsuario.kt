package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class ReconteoUsuario(

        @SerializedName("pendiente")
        var pendiente: kotlin.String,
        @SerializedName("codigoItem")
        var codigoItem: kotlin.String,
        @SerializedName("descripcionItem")
        val descripcionItem: String,
        @SerializedName("barra")
        val barra: String,
        @SerializedName("stock")
        val stock: Int,
        @SerializedName("cinId")
        var cinId: kotlin.Long,
        @SerializedName("itmId")
        val itmId: Long)
