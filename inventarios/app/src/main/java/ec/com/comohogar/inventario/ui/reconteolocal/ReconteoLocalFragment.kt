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
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.dao.ReconteoLocalDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.validacion.ValidacionBarra
import ec.com.comohogar.inventario.validacion.ValidacionCantidad
import ec.com.comohogar.inventario.validacion.ValidacionZona
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReconteoLocalFragment : Fragment(), View.OnKeyListener {

    private lateinit var reconteoLocalViewModel: ReconteoLocalViewModel

    private var editBarra: EditText? = null
    private var editCantidad: EditText? = null
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

        reconteoLocalViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        reconteoLocalViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        reconteoLocalViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        reconteoLocalViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

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
            var existe = verificarExisteCodigo(codigoLeido)
            if(existe) {
                if(codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido) ){
                    editBarra?.error =  getString(R.string.formato_incorrecto)
                }else {
                    editBarra?.error = null
                    editBarra!!.setText(codigoLeido)
                    editCantidad?.requestFocus()
                }
            }else{
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(getString(R.string.item_no_conteo))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Error")
                alert.show()
            }
        } else if (editCantidad!!.hasFocus()) {
            if(editCantidad?.text.toString().isNullOrBlank()) {
                reconteoLocalViewModel?.saltoPorScaneo = false
                editCantidad?.error = getString(R.string.ingrese_cantidad)
                editCantidad?.requestFocus()
            }else if(ValidacionCantidad.validarCantidad(reconteoLocalViewModel.cantidad.value!!.toLong())){
                reconteoLocalViewModel?.saltoPorScaneo = false
                editCantidad?.error =  getString(R.string.error_rango)
                editCantidad?.requestFocus()
            }else{
                reconteoLocalViewModel?.saltoPorScaneo = true
                reconteoLocalViewModel.barraAnterior.value = reconteoLocalViewModel.barra.value
                reconteoLocalViewModel.cantidadAnterior.value = reconteoLocalViewModel.cantidad.value
                guardarConteo()
                var existe = verificarExisteCodigo(codigoLeido)
                if (existe) {
                    if(codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido) ){
                        editBarra?.error =  getString(R.string.formato_incorrecto)
                    }else {
                        reconteoLocalViewModel.barra.value = codigoLeido
                        editCantidad?.requestFocus()
                    }
                } else {
                    editBarra?.error =  getString(R.string.item_no_conteo)
                    reconteoLocalViewModel.barra.value = ""
                    editBarra?.requestFocus()
                }
                editCantidad?.error = null

            }
        }
        reconteoLocalViewModel.cantidad.value = ""
    }

    private fun verificarExisteCodigo(codigoLeido: String): Boolean {
        var existe = false
        var reconteoLocal = sesionAplicacion?.listaReconteoLocal!!.filter { it.barra.equals(codigoLeido) }
        if (reconteoLocal?.isEmpty()) {
            reconteoLocal = sesionAplicacion?.listaReconteoLocal!!.filter { it.codigoItem.equals(codigoLeido) }
            if (!reconteoLocal?.isEmpty()) {
                existe = true
            }
        } else {
            existe = true
        }
        return existe
    }

    fun refrescarEstado(estado: String) {
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP) {
            if (v?.id == R.id.editCantidad) {
                guardarConteo()
            }
        }
        return false
    }
    fun guardarConteo() {
        if(reconteoLocalViewModel?.saltoPorScaneo!!){
            reconteoLocalViewModel?.saltoPorScaneo = false
            insertarConteo()
        }else{
            var guardar: Boolean? = validarCampos()
            if(guardar!!) {
                reconteoLocalViewModel?.barraAnterior.value = reconteoLocalViewModel?.barra.value.toString()
                reconteoLocalViewModel?.cantidadAnterior.value = reconteoLocalViewModel?.cantidad.value.toString()
                reconteoLocalViewModel.barra.value = ""
                reconteoLocalViewModel.cantidad.value = ""
                editBarra?.requestFocus()
                insertarConteo()
            }
        }
    }

    private fun insertarConteo() {
        AsyncTaskGuardarDatosReconteo(this.activity as MainActivity?, this, editCantidad?.text.toString().toInt()).execute()
    }


    private fun validarCampos(): Boolean? {
        var guardar: Boolean? = true
        if (editBarra?.text.isNullOrBlank()) {
            editBarra?.error = getString(R.string.ingrese_barra)
            guardar = false
        }else if((editBarra?.text.toString().contains(" ") )|| ( (!editBarra?.text.toString().contains("-") ) && (!ValidacionBarra.validarFormatoBarra(editBarra?.text.toString()) || !ValidacionBarra.validarEAN13Barra(editBarra?.text.toString())))
            || (editBarra?.text.toString().contains("-") && !ValidacionBarra.validarCodigoInterno(editBarra?.text.toString())))
        {
        //}else if(editBarra?.text.toString().contains(" ") || !ValidacionBarra.validarFormatoBarra(editBarra?.text.toString()) || !ValidacionBarra.validarEAN13Barra(editBarra?.text.toString()) ){
            editBarra?.error =  getString(R.string.formato_incorrecto)
            guardar = false
        } else {
            editBarra?.error = null
        }
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = getString(R.string.ingrese_cantidad)
            guardar = false
        }else if(ValidacionCantidad.validarCantidad(reconteoLocalViewModel.cantidad.value!!.toLong())){
            editCantidad?.error =  getString(R.string.error_rango)
            editCantidad?.requestFocus()
            guardar = false
        } else {
            editCantidad?.error = null
        }
        return guardar
    }

    private fun recuperarReconteo() {
        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, getString(R.string.recuperar_items))
        dialog?.show()

        val inventarioPreferences: SharedPreferences = activity!!.getSharedPreferences(Constantes.PREF_NAME, 0)
        val gson =  Gson()
        val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
        val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)

        val call: Call<List<ReconteoLocal>> = ApiClient.getClient.consultarReconteoUsuario(asignacionUsuario.binId, asignacionUsuario.numeroConteo, sesionAplicacion?.usuId!!.toLong())
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
                            reconteo.fecha = System.currentTimeMillis()
                            reconteoLocalDao?.insertarReconteoLocal(reconteo)
                        }
                    }
                    if(!listaReconteoLocal.isEmpty()) {
                        cargarDatosPantalla()
                    }else{
                        val dialogBuilder = AlertDialog.Builder(activity?.applicationContext!!)

                        dialogBuilder.setMessage(getString(R.string.no_reconteo_pendiente))
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
                dialog?.cancel()
                val dialogBuilder = AlertDialog.Builder(this@ReconteoLocalFragment.activity!!)

                dialogBuilder.setMessage(getString(R.string.error_red))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, id ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Error")
                alert.show()
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
            var conteoDao: ConteoDao? = null
            db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            reconteoLocalDao = db?.reconteoLocalDao()
            conteoDao =  db?.conteoDao()
            val reconteoLocal = sesionAplicacion?.listaReconteoLocal!!.filter { it.barra.equals(reconteoLocalFragmet?.reconteoLocalViewModel?.barraAnterior?.value!!) }
            val reconteoLocalCodigoInterno = sesionAplicacion?.listaReconteoLocal!!.filter { it.codigoItem.equals(reconteoLocalFragmet?.reconteoLocalViewModel?.barraAnterior?.value!!) }

            if(!reconteoLocal?.isEmpty()) {
                reconteoLocal?.get(0).estado= Constantes.ESTADO_PENDIENTE
                reconteoLocalDao?.actualizarConteo(reconteoLocal?.get(0))

                val conteo =  Conteo(barra = reconteoLocalFragmet?.reconteoLocalViewModel?.barraAnterior?.value!!,
                    zona = "-",
                    cantidad = reconteoLocalFragmet?.reconteoLocalViewModel?.cantidadAnterior?.value!!.toInt(),
                    estado = Constantes.ESTADO_PENDIENTE,
                    cinId = reconteoLocal?.get(0).cinId, binId = sesionAplicacion?.binId,
                    numConteo =  sesionAplicacion?.numConteo, usuId = sesionAplicacion?.usuId, fecha = System.currentTimeMillis())
                conteoDao?.insertarConteo(conteo)
            }else  if(!reconteoLocalCodigoInterno?.isEmpty()) {
                reconteoLocalCodigoInterno?.get(0).estado= Constantes.ESTADO_PENDIENTE
                reconteoLocalDao?.actualizarConteo(reconteoLocalCodigoInterno?.get(0))

                val conteo =  Conteo(barra = reconteoLocalFragmet?.reconteoLocalViewModel?.barraAnterior?.value!!,
                    zona = "-",
                    cantidad = reconteoLocalFragmet?.reconteoLocalViewModel?.cantidadAnterior?.value!!.toInt(),
                    estado = Constantes.ESTADO_PENDIENTE,
                    cinId = reconteoLocalCodigoInterno?.get(0).cinId, binId = sesionAplicacion?.binId,
                    numConteo =  sesionAplicacion?.numConteo, usuId = sesionAplicacion?.usuId, fecha = System.currentTimeMillis() )
                conteoDao?.insertarConteo(conteo)

            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            var reconteoLocalAdapter = ReconteoLocalAdapter(activity?.applicationContext!!, sesionAplicacion?.listaReconteoLocal)
            reconteoLocalFragmet?.listview?.adapter  = reconteoLocalAdapter

        }
    }
}