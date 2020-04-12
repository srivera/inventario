package ec.com.comohogar.inventario.validacion

import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.util.Constantes

class ValidacionZona {

    companion object {
        fun validarZona(zona : String, sesionAplicacion: SesionAplicacion): Boolean {
            if(sesionAplicacion.tipoInventario.equals(Constantes.INVENTARIO_BODEGA)){
                val regex = "\\d{4}[-]\\d{2}[-]\\d{1}".toRegex()
                val regexDos = "\\d{2}[-]\\d{2}[-]\\d{1}".toRegex()
                return zona.matches(regex) || zona.matches(regexDos)
            }else{
                val regex = "\\d{4}".toRegex()
                return zona.matches(regex)
            }
        }
    }
}