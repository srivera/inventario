package ec.com.comohogar.inventario.ui.salida

import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class SalirViewModel  : BaseViewModel() {

    var total = MutableLiveData<String>()
    var totalEnviado = MutableLiveData<String>()
    var totalPendiente = MutableLiveData<String>()
}