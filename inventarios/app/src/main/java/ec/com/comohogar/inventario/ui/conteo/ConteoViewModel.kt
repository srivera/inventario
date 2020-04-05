package ec.com.comohogar.inventario.ui.conteo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class ConteoViewModel  : BaseViewModel() {

    var zona = MutableLiveData<String>()
    var barra = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()
    var barraAnterior = MutableLiveData<String>()
    var cantidadAnterior = MutableLiveData<String>()

    var saltoPorScaneo: Boolean? = false


    fun setZona(value: String) {
        this.zona.value = value
    }

    fun setBarra(value: String) {
        this.barra.value = value
    }

    fun setCantidad(value: String) {
        this.cantidad.value = value
    }

    fun setBarraAnterior(value: String) {
        this.barraAnterior.value = value
    }

    fun setCantidadAnterior(value: String) {
        this.cantidadAnterior.value = value
    }

    fun guardarConteo(numeroConteo: Long?, idUsuario: Long?) {
        guardar(numeroConteo, idUsuario)
        if (!saltoPorScaneo!! && !barra.value.toString().equals("")) {
            this.barraAnterior.value = barra.value.toString()
            this.cantidadAnterior.value = cantidad.value.toString()
        }
    }

    fun limpiarFormulario() {
        if(!saltoPorScaneo!!) {
            this.barra.value = ""
        }else{
            saltoPorScaneo = false
        }
        this.cantidad.value = ""
    }

    private fun guardar(numeroConteo: Long?, idUsuario: Long?) {
        var cant: String? = cantidad.value.toString()
        var barr: String? = barra.value.toString()

        if(saltoPorScaneo!!) {

            barr = barraAnterior.value
            cant = cantidadAnterior.value
        }
        Log.i("barra", barr + " / " + cant)
    }

}