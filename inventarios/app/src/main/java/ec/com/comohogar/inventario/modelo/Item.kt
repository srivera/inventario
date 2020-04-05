package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class Item(
        @SerializedName("lugId")
        var lugId: kotlin.Long,
        @SerializedName("forId")
        var forId: kotlin.Long,
        @SerializedName("forNombre")
        val forNombre: String,
        @SerializedName("lugCodigo")
        val lugCodigo: String,
        @SerializedName("lugNombre")
        val lugNombre: String,
        @SerializedName("lugTipo")
        var lugTipo: kotlin.Long,
        @SerializedName("lugDireccion")
        val lugDireccion: String,
        @SerializedName("lugCodigoSri")
        val lugCodigoSri: String)
