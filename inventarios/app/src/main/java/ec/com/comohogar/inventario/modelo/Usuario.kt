package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class Usuario(


        @SerializedName("idUsuario")
        var idUsuario: kotlin.Int,
        @SerializedName("duracionToken")
        var duracionToken: kotlin.Int,
        @SerializedName("idToken")
        val idToken: String,
        @SerializedName("valorToken")
        val valorToken: String,
        @SerializedName("valorTokenRefresh")
        val valorTokenRefresh: String)

