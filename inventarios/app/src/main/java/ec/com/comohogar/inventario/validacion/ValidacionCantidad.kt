package ec.com.comohogar.inventario.validacion

import ec.com.comohogar.inventario.util.Constantes

class ValidacionCantidad {

    companion object {
        fun validarCantidad(cantidad: Int): Boolean {
            return cantidad.compareTo(Constantes.CANTIDAD_MAXIMA) > 0 || cantidad.compareTo(Constantes.CANTIDAD_MINIMA) < 0
        }
    }
}