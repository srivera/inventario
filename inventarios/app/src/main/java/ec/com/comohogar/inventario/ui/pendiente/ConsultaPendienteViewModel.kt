package ec.com.comohogar.inventario.ui.pendiente

import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class ConsultaPendienteViewModel : BaseViewModel() {

    var barra = MutableLiveData<String>()
    var zona = MutableLiveData<String>()

}