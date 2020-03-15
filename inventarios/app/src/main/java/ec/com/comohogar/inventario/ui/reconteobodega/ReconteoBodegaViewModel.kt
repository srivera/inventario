package ec.com.comohogar.inventario.ui.reconteobodega

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReconteoBodegaViewModel : ViewModel() {

    var zonaActual = MutableLiveData<String>()
    var zonaSiguiente = MutableLiveData<String>()
    var item = MutableLiveData<String>()
    var barra = MutableLiveData<String>()
    var descripcion = MutableLiveData<String>()
    var cantidad = MutableLiveData<String>()

    var indice = MutableLiveData<Int>()
    var total = MutableLiveData<Int>()

    var saltoPorScaneo: Boolean? = false


    fun setZonaActual(value: String) {
        this.zonaActual.value = value
    }

    fun setZonaSiguiente(value: String) {
        this.zonaSiguiente.value = value
    }

    fun setItem(value: String) {
        this.item.value = value
    }

    fun setBarra(value: String) {
        this.barra.value = value
    }

    fun setDescripcion(value: String) {
        this.descripcion.value = value
    }

    fun setCantidad(value: String) {
        this.cantidad.value = value
    }

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
        Log.i("barra", barr + " / " + cant)
       /* val call: Call<Long> = ApiClient.getClient.ingresarConteo(178,1144, zona.value!!, barr!!,
            cant!!.toInt()
        )

        call.enqueue(object : Callback<Long> {

            override fun onResponse(call: Call<Long>?, response: Response<Long>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                limpiarFormulario()
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                Log.i("error", "error")
            }

        })*/
    }

}