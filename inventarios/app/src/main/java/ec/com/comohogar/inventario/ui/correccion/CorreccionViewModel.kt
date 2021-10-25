package ec.com.comohogar.inventario.ui.correccion

import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class CorreccionViewModel  : BaseViewModel() {

    var zona = MutableLiveData<String>()
    var barra = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()
    var barraAnterior = MutableLiveData<String>()
    var cantidadAnterior = MutableLiveData<String>()
    var zonaAnterior = MutableLiveData<String>()

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
}