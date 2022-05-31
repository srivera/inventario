package ec.com.comohogar.inventario.ui.reconteobodega

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.R
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.KeyEvent
import android.widget.*
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
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.validacion.ValidacionBarra
import ec.com.comohogar.inventario.validacion.ValidacionCantidad


class ReconteoBodegaFragment : Fragment(), View.OnKeyListener {

    private lateinit var reconteoBodegaViewModel: ReconteoBodegaViewModel

    private var textZonaActual: TextView? = null
    private var textZonaSiguiente: TextView? = null
    private var textItem: TextView? = null
    private var textDescripcion: TextView? = null
    private var textPaginacion: TextView? = null
    private var title: TextView? = null
    private var textStock: TextView? = null

    public var layoutStock: LinearLayout? = null

    private var editCodigoBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var buttonVacio: Button? = null
    private var buttonGuardar: Button? = null

    private var checkBarra: CheckBox? = null

    private var db: InventarioDatabase? = null
    private var reconteoBodegaDao: ReconteoBodegaDao? = null

    private var sesionAplicacion: SesionAplicacion? = null

    private var imgError: ImageView? = null
    private var imgConexion: ImageView? = null

    var dialog: AlertDialog? = null

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
        textPaginacion = root.findViewById(R.id.textPaginacion)
        title = root.findViewById(R.id.title)
        textStock = root.findViewById(R.id.textStock)
        layoutStock = root.findViewById(R.id.layoutStock)

        editCodigoBarra = root.findViewById(R.id.editCodigoBarra)
        editCantidad = root.findViewById(R.id.editCantidadReconteoBodega)

        checkBarra = root.findViewById(R.id.checkBarra)
        buttonVacio = root.findViewById(R.id.buttonVacio)
        buttonGuardar = root.findViewById(R.id.buttonSiguiente)
        imgError = root.findViewById(R.id.imgError)
        imgConexion = root.findViewById(R.id.imgConexion)

        editCantidad?.setOnKeyListener(this)
        editCantidad?.requestFocus()

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
                reconteoBodegaViewModel.barra.value = ""
                editCodigoBarra?.requestFocus()
            } else {
                editCantidad?.requestFocus()
                editCodigoBarra?.setEnabled(false)
                var reconteoBodega: ReconteoBodega
                reconteoBodega = sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)!!
                reconteoBodegaViewModel.barra.value = reconteoBodega.barra
            }
        })


        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
        reconteoBodegaDao = db?.reconteoBodegaDao()
        reconteoBodegaViewModel.indice.value = 0
        reconteoBodegaViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        reconteoBodegaViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        reconteoBodegaViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        reconteoBodegaViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        if(sesionAplicacion?.primeraVez!!) {

            if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                recuperarReconteo()
                layoutStock!!.visibility = View.GONE
            }else{
                recuperarReconteoLocal()
                title!!.text = getString(R.string.reconteo_local)
            }
            sesionAplicacion?.primeraVez = false
        }else{
            cargarDatosPantalla()
        }

        if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
            layoutStock!!.visibility = View.GONE
        }else{
            title!!.text = getString(R.string.reconteo_local)
        }

        refrescarConexion()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        return root
    }

    fun refrescarPantalla(codigoLeido: String) {
        if (editCantidad!!.hasFocus()) {
            if(reconteoBodegaViewModel.cantidad.value.isNullOrBlank()) {
                editCantidad?.error = getString(R.string.ingrese_cantidad)
                editCantidad?.requestFocus()
            }else if(ValidacionCantidad.validarCantidad(reconteoBodegaViewModel.cantidad.value!!.toLong())) {
                editCantidad?.error = getString(R.string.error_rango)
                editCantidad?.requestFocus()
            }else {
                editCantidad?.error = null

                if (codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido)) {
                    val dialogBuilder = AlertDialog.Builder(requireActivity())
                    dialogBuilder.setMessage(getString(R.string.formato_incorrecto))
                        .setCancelable(false)
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                        })

                    val alert = dialogBuilder.create()
                    alert.setTitle("Error")
                    alert.show()
                } else {
                    var reconteoBodega =
                        sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)!!
                    if (codigoLeido.equals(reconteoBodega.barra)) {
                        reconteoBodegaViewModel?.saltoPorScaneo = true
                        reconteoBodegaViewModel.cantidadAnterior.value =
                            reconteoBodegaViewModel.cantidad.value
                        reconteoBodegaViewModel.indiceAnterior.value =
                            reconteoBodegaViewModel.indice.value
                        reconteoBodegaViewModel.cantidad.value = ""
                        guardarReconteoBodega()
                    } else {
                        reconteoBodegaViewModel?.saltoPorScaneo = false
                        val dialogBuilder = AlertDialog.Builder(requireActivity())
                        dialogBuilder.setMessage(getString(R.string.item_escaneado_error))
                            .setCancelable(false)
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Error")
                        alert.show()
                    }
                }
            }
        }
    }

    fun refrescarConexion() {
        val inventarioPreferences: SharedPreferences =
            requireActivity().getSharedPreferences(Constantes.PREF_NAME, 0)
        val conexion = inventarioPreferences.getString(Constantes.URL_CONEXION, "");
        if (conexion.equals(ApiClient.BASE_URL_WIFI)) {
            imgConexion?.setImageResource(R.drawable.wifi)
        } else {
            imgConexion?.setImageResource(R.drawable.internet)
        }
    }

    fun refrescarEstado(estado: String) {
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
        if(!reconteoBodegaViewModel?.saltoPorScaneo!! && !checkBarra?.isChecked!! ){
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setMessage(getString(R.string.error_debe_escanear))
                .setCancelable(false)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                })

            val alert = dialogBuilder.create()
            alert.setTitle("Error")
            alert.show()
        }else {
            if (reconteoBodegaViewModel?.saltoPorScaneo!!) {
                reconteoBodegaViewModel?.saltoPorScaneo = false
                insertarReconteo()
                moverSiguiente()
            } else {
                var guardar: Boolean? = validarCampos()
                if (guardar!!) {
                    reconteoBodegaViewModel.cantidadAnterior.value =
                        reconteoBodegaViewModel.cantidad.value
                    reconteoBodegaViewModel.indiceAnterior.value =
                        reconteoBodegaViewModel.indice.value
                    reconteoBodegaViewModel.cantidad.value = ""
                    insertarReconteo()
                    moverSiguiente()
                }
            }
        }
    }

    fun guardarVacio() {
            reconteoBodegaViewModel.cantidadAnterior.value = "0"
            reconteoBodegaViewModel.indiceAnterior.value = reconteoBodegaViewModel.indice.value
            reconteoBodegaViewModel.cantidad.value = ""
            insertarReconteo()
            moverSiguiente()
    }


    private fun insertarReconteo() {
        AsyncTask.execute {
            db =
                InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            reconteoBodegaDao = db?.reconteoBodegaDao()
            var reconteoBodega: ReconteoBodega
            reconteoBodega =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indiceAnterior.value!!)!!
            reconteoBodega?.cantidad =
                reconteoBodegaViewModel?.cantidadAnterior?.value!!.toInt()
            reconteoBodega?.estado = Constantes.ESTADO_PENDIENTE
            reconteoBodegaDao?.actualizarConteo(reconteoBodega)
        }
    }

    private fun validarCampos(): Boolean? {
        var guardar: Boolean? = true
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = getString(R.string.ingrese_cantidad)
            guardar = false
        }else if(ValidacionCantidad.validarCantidad(reconteoBodegaViewModel.cantidad.value!!.toLong())){
            editCantidad?.error =  getString(R.string.error_rango)
            editCantidad?.requestFocus()
            guardar = false
        } else {
            editCantidad?.error = null
        }
        if (checkBarra?.isChecked!!) {
            var reconteoBodega: ReconteoBodega
            reconteoBodega =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)!!
            if (!reconteoBodegaViewModel.barra.value.equals(reconteoBodega.barra)) {
                if(!reconteoBodegaViewModel.barra.value.equals(reconteoBodega.codigoItem) ) {
                    editCodigoBarra?.error = getString(R.string.codigo_no_corresponde)
                    guardar = false
                }
            }else if(!reconteoBodegaViewModel.barra.value.toString().contains("-")){
                if(!ValidacionBarra.validarFormatoBarra(reconteoBodegaViewModel.barra.value.toString()) || !ValidacionBarra.validarEAN13Barra(reconteoBodegaViewModel.barra.value.toString()) ) {
                    editCodigoBarra?.error = getString(R.string.formato_incorrecto)
                    guardar = false
                }
            }else{
                editCodigoBarra?.error = null
            }
        }
        return guardar
    }

    private fun recuperarReconteo() {

        dialog = ProgressDialog.setProgressDialog(this!!.requireActivity(), getString(R.string.recuperar_items))
        dialog?.show()

        val inventarioPreferences: SharedPreferences = requireActivity().getSharedPreferences(Constantes.PREF_NAME, 0)
        val gson =  Gson()
        val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
        val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)

        val call: Call<List<ReconteoBodega>> = ApiClient.getClient.consultarRutaUsuario(asignacionUsuario.binId, asignacionUsuario.numeroConteo, asignacionUsuario.usuId)
        call.enqueue(object : Callback<List<ReconteoBodega>> {

            @SuppressLint("UseRequireInsteadOfGet")
            override fun onResponse(call: Call<List<ReconteoBodega>>?, response: Response<List<ReconteoBodega>>?) {
                Log.i("respuesta",response!!.body()!!.toString())

                var listaReconteoBodega = response!!.body()

                AsyncTask.execute {
                    reconteoBodegaDao?.eliminarInsertado()
                    for(reconteo in listaReconteoBodega!!){
                        reconteo.estado = Constantes.ESTADO_INSERTADO
                        reconteo.fecha = System.currentTimeMillis()
                        reconteo.pendiente = ""
                        reconteo.noPistoleado = ""
                        reconteoBodegaDao?.insertarReconteoBodega(reconteo)
                        val i = reconteoBodegaDao?.count()
                        Log.i("total", i.toString())
                    }
                    if(!listaReconteoBodega.isEmpty()) {
                        cargarDatosPantalla()
                    }else{
                        dialog?.cancel()
                        this@ReconteoBodegaFragment.activity?.runOnUiThread(java.lang.Runnable {
                            val dialogBuilder = AlertDialog.Builder(this@ReconteoBodegaFragment.activity!!)
                            dialogBuilder.setMessage(getString(R.string.no_reconteo_pendiente))
                                .setCancelable(false)
                                .setPositiveButton("OK", DialogInterface.OnClickListener {
                                        dialog, id ->
                                })

                            val alert = dialogBuilder.create()
                            alert.setTitle(activity?.getString(R.string.informacion))
                            alert.show()
                            buttonGuardar?.isEnabled = false
                            buttonVacio?.isEnabled = false
                            checkBarra?.isEnabled = false
                            editCantidad?.isEnabled = false
                        })

                    }
                }
            }

            @SuppressLint("UseRequireInsteadOfGet")
            override fun onFailure(call: Call<List<ReconteoBodega>>, t: Throwable) {
                Log.i("error", "error")
                dialog?.cancel()
                val dialogBuilder = AlertDialog.Builder(this@ReconteoBodegaFragment.activity!!)

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


    private fun recuperarReconteoLocal() {

        dialog = ProgressDialog.setProgressDialog(this!!.requireActivity(), getString(R.string.recuperar_items))
        dialog?.show()

        val inventarioPreferences: SharedPreferences = requireActivity().getSharedPreferences(Constantes.PREF_NAME, 0)
        val gson =  Gson()
        val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
        val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)

        val call: Call<List<ReconteoBodega>> = ApiClient.getClient.consultarReconteoUsuarioV2(asignacionUsuario.binId, asignacionUsuario.numeroConteo, sesionAplicacion?.usuId!!.toLong())
        call.enqueue(object : Callback<List<ReconteoBodega>> {

            @SuppressLint("UseRequireInsteadOfGet")
            override fun onResponse(call: Call<List<ReconteoBodega>>?, response: Response<List<ReconteoBodega>>?) {
//                Log.i("respuesta",response!!.body()!!.toString())

                var listaReconteoBodega = response!!.body()

                AsyncTask.execute {
                    reconteoBodegaDao?.eliminarInsertado()
                    for(reconteo in listaReconteoBodega!!){
                        reconteo.estado = Constantes.ESTADO_INSERTADO
                        reconteo.fecha = System.currentTimeMillis()
                        reconteo.binId = asignacionUsuario.binId
                        if(reconteo.noPistoleado == null){
                            reconteo.noPistoleado = "N"
                        }
                        reconteoBodegaDao?.insertarReconteoBodega(reconteo)
                        val i = reconteoBodegaDao?.count()
                        Log.i("total", i.toString())
                    }
                    if(!listaReconteoBodega.isEmpty()) {
                        cargarDatosPantalla()
                    }else{
                        dialog?.cancel()
                        this@ReconteoBodegaFragment.activity?.runOnUiThread(java.lang.Runnable {
                            val dialogBuilder = AlertDialog.Builder(this@ReconteoBodegaFragment.activity!!)
                            dialogBuilder.setMessage(getString(R.string.no_reconteo_pendiente))
                                .setCancelable(false)
                                .setPositiveButton("OK", DialogInterface.OnClickListener {
                                        dialog, id ->
                                })

                            val alert = dialogBuilder.create()
                            alert.setTitle(activity?.getString(R.string.informacion))
                            alert.show()
                            buttonGuardar?.isEnabled = false
                            buttonVacio?.isEnabled = false
                            checkBarra?.isEnabled = false
                            editCantidad?.isEnabled = false
                        })

                    }
                }
            }

            @SuppressLint("UseRequireInsteadOfGet")
            override fun onFailure(call: Call<List<ReconteoBodega>>, t: Throwable) {
                Log.i("error", "error")
                dialog?.cancel()
                val dialogBuilder = AlertDialog.Builder(this@ReconteoBodegaFragment.activity!!)

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
        AsyncTaskCargarDatosReconteo(this, reconteoBodegaViewModel).execute()

    }

    fun refrescarError(error: Boolean) {
        if(error) {
            imgError!!.visibility = View.VISIBLE
        }else{
            imgError!!.visibility = View.GONE
        }
    }

    fun moverSiguiente() {
        checkBarra?.isChecked = false
        editCodigoBarra?.isEnabled = false
        reconteoBodegaViewModel.indice.value = reconteoBodegaViewModel.indice.value?.plus(1)
        if(reconteoBodegaViewModel.indice.value!! < sesionAplicacion?.listaReconteoBodega?.size!!) {
            reconteoBodegaViewModel.zonaActual.value =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                    ?.rcoUbicacion
            reconteoBodegaViewModel.descripcion.value =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                    ?.descripcionItem
            reconteoBodegaViewModel.barra.value =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                    ?.barra
            reconteoBodegaViewModel.item.value =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                    ?.codigoItem
            reconteoBodegaViewModel.stock.value =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                    ?.stock.toString()
            reconteoBodegaViewModel.noPistoleado.value =
                sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                    ?.noPistoleado


            if ((sesionAplicacion?.listaReconteoBodega?.size!! - 1) > (reconteoBodegaViewModel.indice.value!!)
            ) {
                var siguienteIndice = reconteoBodegaViewModel.indice.value!! + 1
                reconteoBodegaViewModel.zonaSiguiente.value =
                    sesionAplicacion?.listaReconteoBodega?.get(siguienteIndice)?.rcoUbicacion
            }else{
                reconteoBodegaViewModel.zonaSiguiente.value = ""
            }
            reconteoBodegaViewModel.paginacion.value =
                "Reg. " + (reconteoBodegaViewModel.indice.value!! + 1) + "/" + reconteoBodegaViewModel.total.value.toString()

          //  if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_LOCAL)) {
                if (sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.noPistoleado.equals("S") || (sesionAplicacion?.listaReconteoBodega?.get(
                        reconteoBodegaViewModel.indice.value!!
                    )
                        ?.stockActual)!!.compareTo(0) < 0
                ) {
                    layoutStock!!.visibility = View.VISIBLE
                } else {
                    layoutStock!!.visibility = View.INVISIBLE
                }
           // }
        }else{
            reconteoBodegaViewModel.zonaActual.value = ""
            reconteoBodegaViewModel.descripcion.value = ""
            reconteoBodegaViewModel.barra.value = ""
            reconteoBodegaViewModel.item.value = ""
            reconteoBodegaViewModel.zonaSiguiente.value = ""
            reconteoBodegaViewModel.cantidad.value = ""
            reconteoBodegaViewModel.paginacion.value =  ""
            reconteoBodegaViewModel.noPistoleado.value =  ""
            reconteoBodegaViewModel.stock.value =  "0"
            buttonGuardar?.isEnabled = false
            buttonVacio?.isEnabled = false
            checkBarra?.isEnabled = false
            editCantidad?.isEnabled = false

            val dialogBuilder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setMessage(getString(R.string.termino_reconteo))
                .setCancelable(false)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                })

            val alert = dialogBuilder.create()
            alert.setTitle(activity?.getString(R.string.informacion))
            alert.show()
        }
    }


    class AsyncTaskCargarDatosReconteo(private var reconteoBodegaFragment: ReconteoBodegaFragment?, var reconteoBodegaViewModel: ReconteoBodegaViewModel) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null

        @SuppressLint("UseRequireInsteadOfGet")
        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = reconteoBodegaFragment?.activity?.applicationContext as SesionAplicacion?
            var db: InventarioDatabase? = null
            var reconteoBodegaDao: ReconteoBodegaDao? = null
            db = InventarioDatabase.getInventarioDataBase(context = reconteoBodegaFragment?.activity?.applicationContext!!)
            reconteoBodegaDao = db?.reconteoBodegaDao()
            sesionAplicacion?.listaReconteoBodega = reconteoBodegaDao?.getReconteosBodegaInsertado()
            return reconteoBodegaDao?.countInsertado()
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            if(sesionAplicacion?.listaReconteoBodega?.size!! > 0) {
                reconteoBodegaViewModel.total.value = result
                reconteoBodegaViewModel.zonaActual.value =
                    sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.rcoUbicacion
                reconteoBodegaViewModel.descripcion.value =
                    sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.descripcionItem
                reconteoBodegaViewModel.barra.value =
                    sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.barra
                reconteoBodegaViewModel.item.value =
                    sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.codigoItem
                reconteoBodegaViewModel.stock.value =
                    sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.stock.toString()
                reconteoBodegaViewModel.noPistoleado.value =
                    sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.noPistoleado

                if ((sesionAplicacion?.listaReconteoBodega?.size!! - 1) >= (reconteoBodegaViewModel.indice.value?.plus(
                        1
                    )!!)
                ) {
                    var siguienteIndice = reconteoBodegaViewModel.indice.value!! + 1
                    reconteoBodegaViewModel.zonaSiguiente.value =
                        sesionAplicacion?.listaReconteoBodega?.get(siguienteIndice)?.rcoUbicacion
                }else{
                    reconteoBodegaViewModel.zonaSiguiente.value = ""
                }
                reconteoBodegaViewModel.paginacion.value =
                    "Reg. " + (reconteoBodegaViewModel.indice.value!! + 1) + "/" + reconteoBodegaViewModel.total.value.toString()

                //if()
                if( sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.noPistoleado.equals("S") || (sesionAplicacion?.listaReconteoBodega?.get(reconteoBodegaViewModel.indice.value!!)
                        ?.stockActual)!!.compareTo(0) < 0) {
                    reconteoBodegaFragment!!.layoutStock!!.visibility = View.VISIBLE
                }else{
                    reconteoBodegaFragment!!.layoutStock!!.visibility = View.INVISIBLE
                }
            }else{
                reconteoBodegaViewModel.zonaActual.value = ""
                reconteoBodegaViewModel.descripcion.value = ""
                reconteoBodegaViewModel.barra.value = ""
                reconteoBodegaViewModel.item.value = ""
                reconteoBodegaViewModel.zonaSiguiente.value = ""
                reconteoBodegaViewModel.cantidad.value = ""
                reconteoBodegaViewModel.paginacion.value =  ""
                reconteoBodegaViewModel.noPistoleado.value =  ""
                reconteoBodegaViewModel.stock.value =  "0"
                reconteoBodegaFragment?.buttonGuardar?.isEnabled = false
                reconteoBodegaFragment?.buttonVacio?.isEnabled = false
                reconteoBodegaFragment?.checkBarra?.isEnabled = false
                reconteoBodegaFragment?.editCantidad?.isEnabled = false

                val dialogBuilder = AlertDialog.Builder(reconteoBodegaFragment?.requireActivity()!!.parent)
                dialogBuilder.setMessage(reconteoBodegaFragment?.getString(R.string.termino_reconteo))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(reconteoBodegaFragment?.getString(R.string.informacion))
                alert.show()
            }
            reconteoBodegaFragment?.dialog?.cancel()
        }
    }
}