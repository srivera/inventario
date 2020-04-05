package ec.com.comohogar.inventario.ui.reconteolocal

import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class ReconteoLocalViewModel : BaseViewModel() {

    var barra = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()
    var barraAnterior = MutableLiveData<String>()
    var cantidadAnterior = MutableLiveData<String>()

    var saltoPorScaneo: Boolean? = false

    fun guardarConteo() {
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

}