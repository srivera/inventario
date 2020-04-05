package ec.com.comohogar.inventario.ui.reconteolocal

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.R
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.*
import android.widget.*
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.databinding.FragmentReconteoLocalBinding
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.adapter.ReconteoLocalAdapter
import ec.com.comohogar.inventario.modelo.AsignacionUsuario
import ec.com.comohogar.inventario.persistencia.dao.ReconteoLocalDao
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReconteoLocalFragment : Fragment(), View.OnKeyListener {

    private lateinit var reconteoLocalViewModel: ReconteoLocalViewModel

    private var editBarra: EditText? = null
    private var editCantidad: EditText? = null
    private var textEstado: TextView? = null
    private var buttonGuardar: ImageButton? = null
    var listview: ListView? = null

    private var db: InventarioDatabase? = null
    private var reconteoLocalDao: ReconteoLocalDao? = null
    private var sesionAplicacion: SesionAplicacion? = null

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentReconteoLocalBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_reconteo_local, container, false
        )
        binding.setLifecycleOwner(this)
        reconteoLocalViewModel = ViewModelProviders.of(this).get(ReconteoLocalViewModel::class.java)
        binding.uiController = this
        binding.reconteoLocalViewModel = reconteoLocalViewModel
        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
        reconteoLocalDao = db?.reconteoLocalDao()

        editBarra = root.findViewById(R.id.editBarra)
        editCantidad = root.findViewById(R.id.editCantidad)
        textEstado = root.findViewById(R.id.textEstado)
        buttonGuardar = root.findViewById(R.id.buttonGuardar)
        editCantidad?.setOnKeyListener(this)
        listview = root.findViewById(R.id.listview)

        editCantidad?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }

        }
        editBarra?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

        reconteoLocalViewModel.inventario.value = "Inventario: " + sesionAplicacion?.binId.toString()
        reconteoLocalViewModel.conteo.value = " Conteo: " + sesionAplicacion?.cinId.toString()
        reconteoLocalViewModel.numconteo.value = " Número: " + sesionAplicacion?.numConteo.toString()
        reconteoLocalViewModel.usuario.value = " Usuario: " + sesionAplicacion?.empleado?.empId.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        if(sesionAplicacion?.primeraVez!!) {
            recuperarReconteo()
            sesionAplicacion?.primeraVez = false
        }else{
            var reconteoLocalAdapter = ReconteoLocalAdapter(activity?.applicationContext!!, sesionAplicacion?.listaReconteoLocal)
            listview?.adapter  = reconteoLocalAdapter
        }

        return root
    }

    fun refrescarPantalla(codigoLeido: String) {
        if (editBarra!!.hasFocus()) {
            editBarra!!.setText(codigoLeido)
            editCantidad?.requestFocus()
        } else if (editCantidad!!.hasFocus()) {
            reconteoLocalViewModel.saltoPorScaneo = true
            reconteoLocalViewModel.barra.value = codigoLeido
            reconteoLocalViewModel.cantidad.value = ""
            editBarra!!.setText(codigoLeido)
            editCantidad?.requestFocus()
            guardarConteo(codigoLeido)
        }
    }

    fun refrescarEstado(estado: String) {
        textEstado?.setText(estado)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
            if (v?.id == R.id.editCantidad) {
                //guardarConteo()

            }
        }
        return false
    }

    fun guardarConteo(codigoBarra: String) {
        var guardar: Boolean? = true
        if (editBarra?.text.isNullOrBlank()) {
            editBarra?.error = "Ingrese la barra"
            guardar = false
        } else {
            editBarra?.error = null
        }
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = "Ingrese la cantidad"
            guardar = false
        } else {
            editCantidad?.error = null
        }
        if (guardar!!) {
            AsyncTaskGuardarDatosReconteo(this.activity as MainActivity?, this,  editCantidad?.text.toString().toInt()).execute()
        }

        reconteoLocalViewModel.guardarConteo()
    }

    fun guardarConteo() {
        var guardar: Boolean? = true
        if (editBarra?.text.isNullOrBlank()) {
            editBarra?.error = "Ingrese la barra"
            guardar = false
        } else {
            editBarra?.error = null
        }
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = "Ingrese la cantidad"
            guardar = false
        } else {
            editCantidad?.error = null
        }
        if (guardar!!) {
            AsyncTaskGuardarDatosReconteo(this.activity as MainActivity?, this, editCantidad?.text.toString().toInt()).execute()
        }
        reconteoLocalViewModel.guardarConteo()
    }

    private fun recuperarReconteo() {
        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, "Recuperando ítems...")
        dialog?.show()

        val inventarioPreferences: SharedPreferences = activity!!.getSharedPreferences(Constantes.PREF_NAME, 0)
        val gson =  Gson()
        val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
        val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)

        val call: Call<List<ReconteoLocal>> = ApiClient.getClient.consultarReconteoUsuario(asignacionUsuario.binId, asignacionUsuario.numeroConteo, asignacionUsuario.usuId)
        call.enqueue(object : Callback<List<ReconteoLocal>> {

            override fun onResponse(call: Call<List<ReconteoLocal>>?, response: Response<List<ReconteoLocal>>?) {
                Log.i("respuesta",response!!.body()!!.toString())

                var listaReconteoLocal = response!!.body()

                AsyncTask.execute {
                    reconteoLocalDao?.eliminar()
                    var items = reconteoLocalDao?.getReconteosLocal()
                    for(reconteo in listaReconteoLocal!!){
                        val filtered = items?.filter {
                            it.codigoItem.equals(reconteo.codigoItem)
                        }
                        if(filtered == null || filtered.isEmpty()) {
                            reconteo.estado = Constantes.ESTADO_INSERTADO
                            reconteoLocalDao?.insertarReconteoLocal(reconteo)
                            val i = reconteoLocalDao?.count()
                        }
                    }
                    if(!listaReconteoLocal.isEmpty()) {
                        cargarDatosPantalla()
                    }else{
                        val dialogBuilder = AlertDialog.Builder(activity?.applicationContext!!)

                        dialogBuilder.setMessage("No tiene pendientes de recontar.")
                            .setCancelable(false)
                            .setPositiveButton("OK", DialogInterface.OnClickListener {
                                    dialog, id ->
                                dialog.cancel()
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Error")
                        alert.show()
                    }
                }
            }

            override fun onFailure(call: Call<List<ReconteoLocal>>, t: Throwable) {
                Log.i("error", "error")
            }

        })
    }


    private fun cargarDatosPantalla() {
        AsyncTaskCargarDatosReconteo(this.activity as MainActivity?, this).execute()
    }

    class AsyncTaskCargarDatosReconteo(private var activity: MainActivity?, var reconteoLocalFragmet: ReconteoLocalFragment?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            var db: InventarioDatabase? = null
            var reconteoLocalDao: ReconteoLocalDao? = null
            db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            reconteoLocalDao = db?.reconteoLocalDao()
            sesionAplicacion?.listaReconteoLocal = reconteoLocalDao?.getReconteosLocal()
            return reconteoLocalDao?.count()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            reconteoLocalFragmet?.dialog?.cancel()
            var reconteoLocalAdapter = ReconteoLocalAdapter(activity?.applicationContext!!, sesionAplicacion?.listaReconteoLocal)
            reconteoLocalFragmet?.listview?.adapter  = reconteoLocalAdapter
        }
    }


    class AsyncTaskGuardarDatosReconteo(private var activity: MainActivity?, var reconteoLocalFragmet: ReconteoLocalFragment?, var cantidad: Int?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            var db: InventarioDatabase? = null
            var reconteoLocalDao: ReconteoLocalDao? = null
            db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            reconteoLocalDao = db?.reconteoLocalDao()
            val reconteoLocal = sesionAplicacion?.listaReconteoLocal!!.filter { it.barra.equals(reconteoLocalFragmet?.editBarra?.text.toString()) }
            if(!reconteoLocal?.isEmpty()) {
                reconteoLocal?.get(0).cantidad= this!!.cantidad!!
                reconteoLocal?.get(0).estado= Constantes.ESTADO_PENDIENTE
                reconteoLocalDao?.actualizarConteo(reconteoLocal?.get(0))
            }
            return reconteoLocalDao?.count()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            var reconteoLocalAdapter = ReconteoLocalAdapter(activity?.applicationContext!!, sesionAplicacion?.listaReconteoLocal)
            reconteoLocalFragmet?.listview?.adapter  = reconteoLocalAdapter

        }
    }
}