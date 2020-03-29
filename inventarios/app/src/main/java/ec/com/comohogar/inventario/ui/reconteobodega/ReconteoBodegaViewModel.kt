package ec.com.comohogar.inventario.ui.reconteobodega


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReconteoBodegaViewModel : ViewModel() {

    var zonaActual = MutableLiveData<String>()
    var zonaSiguiente = MutableLiveData<String>()
    var item = MutableLiveData<String>()
    var barra = MutableLiveData<String>()
    var descripcion = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()
    var inventario = MutableLiveData<String>()
    var conteo = MutableLiveData<String>()
    var usuario = MutableLiveData<String>()
    var numconteo = MutableLiveData<String>()

    var indice = MutableLiveData<Int>()
    var total = MutableLiveData<Int>()

    var saltoPorScaneo: Boolean? = false


    fun guardarConteo() {
        guardar()
        if (!saltoPorScaneo!! && !barra.value.toString().equals("")) {

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

    private fun guardar() {
        var cant: String? = cantidad.value.toString()
        var barr: String? = barra.value.toString()

        if(saltoPorScaneo!!) {


        }
    }

}