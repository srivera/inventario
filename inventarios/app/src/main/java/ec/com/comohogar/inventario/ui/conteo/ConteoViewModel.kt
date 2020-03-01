package ec.com.comohogar.inventario.ui.conteo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConteoViewModel : ViewModel() {

    var zona = MutableLiveData<String>()
    var barra = MutableLiveData<String>()
    var cantidad = MutableLiveData<Int>()
    var barraAnterior = MutableLiveData<String>()
    var cantidadAnterior = MutableLiveData<Int>()

    fun setZona(value: String) {
        this.zona.value = value
    }

    fun setBarra(value: String) {
        this.barra.value = value
    }

    fun setCantidad(value: Int) {
        this.cantidad.value = value
    }

    fun setBarraAnterior(value: String) {
        this.barraAnterior.value = value
    }

    fun setCantidadAnterior(value: Int) {
        this.cantidadAnterior.value = value
    }

    fun guardarConteo() {
        if (!barra.value.toString().equals("")) {
            this.barraAnterior.value = barra.value.toString()
        }
    }
}