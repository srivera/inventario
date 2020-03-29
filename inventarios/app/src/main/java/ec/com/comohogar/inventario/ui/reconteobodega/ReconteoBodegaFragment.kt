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
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.KeyEvent
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.SesionAplicacion
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
    private var textPaginacion: TextView? = null

    private var editCodigoBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var buttonVacio: Button? = null
    private var buttonGuardar: Button? = null

    private var checkBarra: CheckBox? = null

    private var db: InventarioDatabase? = null
    private var reconteoBodegaDao: ReconteoBodegaDao? = null

    private var sesionAplicacion: SesionAplicacion? = null

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
        textPaginacion = root.findViewById(R.id.textPaginacion)

        editCodigoBarra = root.findViewById(R.id.editCodigoBarra)
        editCantidad = root.findViewById(R.id.editCantidadReconteoBodega)

        checkBarra = root.findViewById(R.id.checkBarra)
        buttonVacio = root.findViewById(R.id.buttonVacio)
        buttonGuardar = root.findViewById(R.id.buttonSiguiente)

        editCantidad?.setOnKeyListener(this)

        editCantidad?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        checkBarra?.setOnClickListener(View.OnClickListener {
            if (checkBarra?.isChecked!!) {
                editCodigoBarra?.setEnabled(true)
                buttonGuardar?.setEnabled(true)
                reconteoBodegaViewModel.barra.value = ""
            } else {
                editCodigoBarra?.setEnabled(false)
                buttonGuardar?.setEnabled(false)
                var reconteoBodega: ReconteoBodega
                reconteoBodega = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)!!
                reconteoBodegaViewModel.barra.value = reconteoBodega.barra
            }
        })


        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
        reconteoBodegaDao = db?.reconteoBodegaDao()
        reconteoBodegaViewModel.indice.value = 0
        reconteoBodegaViewModel.inventario.value = "Inventario: " + sesionAplicacion?.binId.toString()
        reconteoBodegaViewModel.conteo.value = " Conteo: " + sesionAplicacion?.cinId.toString()
        reconteoBodegaViewModel.numconteo.value = " Número: " + sesionAplicacion?.numConteo.toString()
        reconteoBodegaViewModel.usuario.value = " Usuario: " + sesionAplicacion?.empleado?.empId.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

       recuperarReconteo()

        return root
    }

    fun refrescarPantalla(codigoLeido: String) {
        Log.i("fragment", "fragment")
        if (editCantidad!!.hasFocus()) {
            guardarReconteoBodega()
            reconteoBodegaViewModel.cantidad.value = ""

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
            AsyncTask.execute {
                db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
                reconteoBodegaDao = db?.reconteoBodegaDao()
                var reconteoBodega: ReconteoBodega
                reconteoBodega = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)!!
                reconteoBodega?.estado = Constantes.ESTADO_PENDIENTE
                reconteoBodegaDao?.actualizarConteo(reconteoBodega)
            }
        }
        moverSiguiente()

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
                    reconteoBodegaDao?.eliminar()
                    for(reconteo in listaReconteoBodega!!){
                        reconteo.estado = Constantes.ESTADO_INSERTADO
                        reconteoBodegaDao?.insertarReconteoBodega(reconteo)
                        val i = reconteoBodegaDao?.count()
                        Log.i("total", i.toString())
                    }
                    if(!listaReconteoBodega.isEmpty()) {
                        cargarDatosPantalla()
                    }else{
                        val dialogBuilder = AlertDialog.Builder(activity?.applicationContext!!)

                        dialogBuilder.setMessage("No tiene pendientes de recontar.")
                            .setCancelable(false)
                            .setPositiveButton("OK", DialogInterface.OnClickListener {
                                    dialog, id ->
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Error")
                        alert.show()
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

    }

    fun moverSiguiente() {
        reconteoBodegaViewModel.indice.value = reconteoBodegaViewModel.indice.value?.plus(1)
        reconteoBodegaViewModel.zonaActual.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.rcoUbicacion
        reconteoBodegaViewModel.descripcion.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.descripcionItem
        reconteoBodegaViewModel.barra.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.barra
        reconteoBodegaViewModel.item.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.codigoItem

        if(sesionAplicacion?.listaReconteoBodega?.size!! >= (reconteoBodegaViewModel.indice.value?.plus(1)!!)){
            reconteoBodegaViewModel.zonaSiguiente.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!.plus(1))?.rcoUbicacion
        }
        textPaginacion?.text = "Reg. " + reconteoBodegaViewModel.indice.value.toString() + "/" + reconteoBodegaViewModel.total.value.toString()
    }


    class AsyncTaskCargarDatosReconteo(private var activity: MainActivity?, var reconteoBodegaViewModel: ReconteoBodegaViewModel) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null

        override fun onPreExecute() {
            super.onPreExecute()
            activity?.progressBar?.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            var db: InventarioDatabase? = null
            var reconteoBodegaDao: ReconteoBodegaDao? = null
            db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            reconteoBodegaDao = db?.reconteoBodegaDao()
            sesionAplicacion?.listaReconteoBodega = reconteoBodegaDao?.getReconteosBodega()

            return reconteoBodegaDao?.count()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            reconteoBodegaViewModel.total.value = result
            activity?.progressBar?.visibility = View.INVISIBLE
            reconteoBodegaViewModel.zonaActual.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.rcoUbicacion
            reconteoBodegaViewModel.descripcion.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.descripcionItem
            reconteoBodegaViewModel.barra.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.barra
            reconteoBodegaViewModel.item.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)?.codigoItem

            if(sesionAplicacion?.listaReconteoBodega?.size!! >= (reconteoBodegaViewModel.indice.value?.plus(1)!!)){
                reconteoBodegaViewModel.zonaSiguiente.value = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!.plus(1))?.rcoUbicacion
            }
        }
    }
}