package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class UsuarioResponse(
    @SerializedName("usu_id") var idUsuario: String, @SerializedName("emp_nombre") var nombre: String, @SerializedName(
        "emp_apellido"
    ) var apellido: String
) {
}