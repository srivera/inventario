package ec.com.comohogar.inventario.ui.conteo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ec.com.comohogar.inventario.modelo.UsuarioResponse
import ec.com.comohogar.inventario.webservice.APIService
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
        login()
        if (!barra.value.toString().equals("")) {
            this.barraAnterior.value = barra.value.toString()
        }
    }

    private fun login() {
        doAsync {
            val call = getRetrofit().create(APIService::class.java).login().execute()
            val usuario = call.body() as UsuarioResponse
            uiThread {
                if(usuario.idUsuario != null) {

                }else{

                }
            }
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://app.sukasa.com:8080/erp-rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}