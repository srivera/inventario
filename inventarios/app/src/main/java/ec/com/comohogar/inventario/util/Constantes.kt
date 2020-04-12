package ec.com.comohogar.inventario.util

class Constantes {

    companion object {
        const val PREF_NAME = "INVENTARIOS_PREFERENCES"
        const val EMPLEADO = "EMPLEADO"
        const val ASIGNACION_USUARIO = "ASIGNACION_USUARIO"
        const val TIPO_INVENTARIO = "TIPO_INVENTARIO"
        const val LOCAL = "LOCAL"
        const val INVENTARIO = "INVENTARIO"
        const val CONTEO = "CONTEO"
        const val ES_CONTEO_RECONTEO = "ES_CONTEO_RECONTEO"
        const val ES_CONTEO = "CONTEO"
        const val ES_RECONTEO = "RECONTEO"

        const val TITULO_INICIAL = "Tipo de Conteo"

        //Tipo de inventario
        const val INVENTARIO_BODEGA = 2
        const val INVENTARIO_LOCAL = 1

        //Estados de envío
        const val ESTADO_PENDIENTE = "PEN"
        const val ESTADO_ENVIADO = "ENV"
        const val ESTADO_ERROR = "ERR"
        const val ESTADO_INSERTADO = "INS"

        //Rango cantidades
        const val CANTIDAD_MAXIMA = 25000
        const val CANTIDAD_MINIMA = -25000
    }

}