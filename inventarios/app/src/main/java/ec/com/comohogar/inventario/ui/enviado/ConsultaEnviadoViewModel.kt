package ec.com.comohogar.inventario.ui.enviado

import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class ConsultaEnviadoViewModel : BaseViewModel() {

    var barra = MutableLiveData<String>()
    var zona = MutableLiveData<String>()

}