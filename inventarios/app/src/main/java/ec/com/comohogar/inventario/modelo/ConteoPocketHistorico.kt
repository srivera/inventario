package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class ConteoPocketHistorico(

        @SerializedName("barra")
        var barra: String,
        @SerializedName("zona")
        var zona: kotlin.String,
        @SerializedName("cantidad")
        val cantidad: Int)
