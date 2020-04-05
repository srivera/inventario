package ec.com.comohogar.inventario.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {

    var inventario = MutableLiveData<String>()
    var conteo = MutableLiveData<String>()
    var usuario = MutableLiveData<String>()
    var numconteo = MutableLiveData<String>()

}
