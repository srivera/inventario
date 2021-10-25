package ec.com.comohogar.inventario.modelo

import com.google.gson.annotations.SerializedName

data class Empleado(

        @SerializedName("vwCUEmplId")
        var vwCUEmplId: kotlin.Long,
        @SerializedName("usuId")
        var usuId: kotlin.Long,
        @SerializedName("usuEstado")
        val usuEstado: Int,
        @SerializedName("empId")
        val empId: Long,
        @SerializedName("empNombreCompleto")
        val empNombreCompleto: String,
        @SerializedName("empNombre")
        var empNombre: kotlin.String,
        @SerializedName("empApellido")
        val empApellido: String,
        @SerializedName("empEstado")
        val empEstado: Int,
        @SerializedName("usuUsuario")
        var usuUsuario: kotlin.String,
        @SerializedName("usuClave")
        var usuClave: kotlin.String,
        @SerializedName("grpNombre")
        val grpNombre: String,
        @SerializedName("grpId")
        val grpId: Long,
        @SerializedName("empCodigo")
        val empCodigo: String,
        @SerializedName("empMail")
        var empMail: kotlin.String,
        @SerializedName("empExterno")
        val empExterno: Int,
        @SerializedName("rrhEmpleado")
        val rrhEmpleado: String,
        @SerializedName("codigoUsuario")
        var codigoUsuario: kotlin.String,
        @SerializedName("lugId")
        val lugId: Int,
        @SerializedName("usuUtilizaPocket")
        val usuUtilizaPocket: Int,
        @SerializedName("esAuditor")
        var esAuditor: kotlin.String)
