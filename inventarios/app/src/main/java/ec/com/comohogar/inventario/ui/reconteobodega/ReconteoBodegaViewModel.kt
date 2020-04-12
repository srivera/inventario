package ec.com.comohogar.inventario.ui.reconteobodega


import androidx.lifecycle.MutableLiveData
import ec.com.comohogar.inventario.ui.base.BaseViewModel

class ReconteoBodegaViewModel : BaseViewModel() {

    var zonaActual = MutableLiveData<String>()
    var zonaSiguiente = MutableLiveData<String>()
    var item = MutableLiveData<String>()
    var barra = MutableLiveData<String>()
    var descripcion = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()
    var paginacion = MutableLiveData<String>()
    var cantidadAnterior = MutableLiveData<String>()

    var indiceAnterior = MutableLiveData<Int>()
    var indice = MutableLiveData<Int>()

    var total = MutableLiveData<Int>()

    var saltoPorScaneo: Boolean? = false

}