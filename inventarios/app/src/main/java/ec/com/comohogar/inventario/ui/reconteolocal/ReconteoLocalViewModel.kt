package ec.com.comohogar.inventario.ui.reconteolocal

import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class ReconteoLocalViewModel : BaseViewModel() {

    var barra = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()
    var barraAnterior = MutableLiveData<String>()
    var cantidadAnterior = MutableLiveData<String>()

    var saltoPorScaneo: Boolean? = false

}