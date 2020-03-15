package ec.com.comohogar.inventario.ui.reconteobodega

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.R
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.KeyEvent
import com.google.gson.Gson
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.databinding.FragmentReconteoBodegaBinding
import ec.com.comohogar.inventario.modelo.AsignacionUsuario
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.dao.ReconteoBodegaDao
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega


class ReconteoBodegaFragment : Fragment(), View.OnKeyListener {

    private lateinit var reconteoBodegaViewModel: ReconteoBodegaViewModel

    private var textZonaActual: TextView? = null
    private var textZonaSiguiente: TextView? = null
    private var textItem: TextView? = null
    private var textDescripcion: TextView? = null
    private var textEstado: TextView? = null

    private var editCodigoBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var buttonVacio: Button? = null
    private var buttonSiguiente: Button? = null

    private var db: InventarioDatabase? = null
    private var reconteoBodegaDao: ReconteoBodegaDao? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentReconteoBodegaBinding

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_reconteo_bodega, container, false
        )

        binding.setLifecycleOwner(this)

        reconteoBodegaViewModel = ViewModelProviders.of(this).get(ReconteoBodegaViewModel::class.java)

        binding.uiController = this

        binding.reconteoBodegaViewModel = reconteoBodegaViewModel

        val root = binding.getRoot()

        textZonaActual = root.findViewById(R.id.textZonaActual)
        textZonaSiguiente = root.findViewById(R.id.textZonaSiguiente)
        textItem = root.findViewById(R.id.textItem)
        textDescripcion = root.findViewById(R.id.textDescripcion)
        textEstado = root.findViewById(R.id.textEstado)

        editCodigoBarra = root.findViewById(R.id.editCodigoBarra)
        editCantidad = root.findViewById(R.id.editCantidad)

        buttonVacio = root.findViewById(R.id.buttonVacio)
        buttonSiguiente = root.findViewById(R.id.buttonSiguiente)

        editCantidad?.setOnKeyListener(this)

        editCantidad?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
        reconteoBodegaDao = db?.reconteoBodegaDao()
        reconteoBodegaViewModel.indice.value = 1
        cargarDatosPantalla()
    //    recuperarReconteo()

        return root
    }


    fun refrescarPantalla(codigoLeido: String) {
        Log.i("fragment", "fragment")
        if (editCantidad!!.hasFocus()) {
            guardarReconteoBodega()
        }
    }

    fun refrescarEstado(estado: String) {
        textEstado?.setText(estado)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
            if (v?.id == R.id.editCantidad) {
                guardarReconteoBodega()

            }
        }
        return false
    }

    fun guardarReconteoBodega() {
        var guardar: Boolean? = true
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = "Ingrese la cantidad"
            guardar = false
        } else {
            editCantidad?.error = null
        }
        if (guardar!!) {

        }
        reconteoBodegaViewModel.guardarConteo()
    }

    private fun recuperarReconteo() {

        val inventarioPreferences: SharedPreferences = activity!!.getSharedPreferences(Constantes.PREF_NAME, 0)
        val gson =  Gson()
        val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
        val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)

        val call: Call<List<ReconteoBodega>> = ApiClient.getClient.consultarRutaUsuario(asignacionUsuario.binId, asignacionUsuario.numeroConteo, asignacionUsuario.usuId)
        call.enqueue(object : Callback<List<ReconteoBodega>> {

            override fun onResponse(call: Call<List<ReconteoBodega>>?, response: Response<List<ReconteoBodega>>?) {
                Log.i("respuesta",response!!.body()!!.toString())

                var listaReconteoBodega = response!!.body()

                AsyncTask.execute {
                    for(reconteo in listaReconteoBodega!!){
                        reconteoBodegaDao?.insertarReconteoBodega(reconteo)
                        val i = reconteoBodegaDao?.count()
                    }
                }
            }

            override fun onFailure(call: Call<List<ReconteoBodega>>, t: Throwable) {
                Log.i("error", "error")
            }

        })
    }

    private fun cargarDatosPantalla() {
        AsyncTaskCargarDatosReconteo(this.activity as MainActivity?, reconteoBodegaViewModel).execute()
        /* AsyncTask.execute {
             var listaReconteoBodega = reconteoBodegaDao?.getReconteosBodega()
             reconteoBodegaViewModel.zonaActual.value = listaReconteoBodega?.get(
                 reconteoBodegaViewModel.indice.value!!
             )
                 ?.rcoUbicacion

         }*/

    }

    private fun moverSiguiente() {

    }


    class AsyncTaskCargarDatosReconteo(private var activity: MainActivity?, var reconteoBodegaViewModel: ReconteoBodegaViewModel) : AsyncTask<String, String, String>() {

        var listaReconteoBodega: List<ReconteoBodega>? = null

        override fun onPreExecute() {
            super.onPreExecute()
            activity?.progressBar?.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: String?): String {
            listaReconteoBodega = ArrayList<ReconteoBodega>()
            var db: InventarioDatabase? = null
            var reconteoBodegaDao: ReconteoBodegaDao? = null
            db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            reconteoBodegaDao = db?.reconteoBodegaDao()
            listaReconteoBodega = reconteoBodegaDao?.getReconteosBodega()
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            activity?.progressBar?.visibility = View.INVISIBLE
            reconteoBodegaViewModel.zonaActual.value = listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.rcoUbicacion
            reconteoBodegaViewModel.descripcion.value = listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.descripcionItem
            reconteoBodegaViewModel.barra.value = listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.barra
            reconteoBodegaViewModel.item.value = listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.codigoItem
        }
    }
}