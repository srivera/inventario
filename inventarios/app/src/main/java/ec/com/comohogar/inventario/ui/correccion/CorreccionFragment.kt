package ec.com.comohogar.inventario.ui.correccion


import android.os.Bundle
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
import android.view.KeyEvent
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import android.os.AsyncTask
import android.widget.ImageView
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.databinding.FragmentCorreccionBinding
import ec.com.comohogar.inventario.modelo.ConteoPendiente
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.validacion.ValidacionBarra
import ec.com.comohogar.inventario.validacion.ValidacionCantidad
import ec.com.comohogar.inventario.validacion.ValidacionZona
import ec.com.comohogar.inventario.webservice.ApiClient


class CorreccionFragment : Fragment(), View.OnKeyListener {

    private lateinit var correccionViewModel: CorreccionViewModel


    private var editZona: EditText? = null
    private var editBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var textBarraAnterior: TextView? = null
    private var textCantidadAnterior: TextView? = null
    private var textZonaAnterior: TextView? = null

    private var imgError: ImageView? = null
    private var imgConexion: ImageView? = null

    private var buttonGuardar: Button? = null

    private var db: InventarioDatabase? = null
    private var conteoDao: ConteoDao? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentCorreccionBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_correccion, container, false
        )
        binding.setLifecycleOwner(this)
        correccionViewModel = ViewModelProviders.of(this).get(CorreccionViewModel::class.java)
        binding.uiController = this
        binding.correccionViewModel = correccionViewModel
        val root = binding.getRoot()

        editZona = root.findViewById(R.id.editZona)
        editBarra = root.findViewById(R.id.editBarra)
        editCantidad = root.findViewById(R.id.editCantidad)
        imgError = root.findViewById(R.id.imgError)
        imgConexion = root.findViewById(R.id.imgConexion)

        textBarraAnterior = root.findViewById(R.id.textBarraAnterior)
        textCantidadAnterior = root.findViewById(R.id.textCantidadAnterior)
        textZonaAnterior = root.findViewById(R.id.textZonaAnterior)

        buttonGuardar = root.findViewById(R.id.buttonGuardar)



        editCantidad?.setOnKeyListener(this)

        editCantidad?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }

        }
        editBarra?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }
        editZona?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                correccionViewModel.zona.value = ""
                val imm = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

        correccionViewModel.inventario.value = getString(R.string.etiqueta_inventario) + (activity as MainActivity).sesionAplicacion?.binId.toString()
        correccionViewModel.conteo.value = getString(R.string.etiqueta_conteo) + (activity as MainActivity).sesionAplicacion?.cinId.toString()
        correccionViewModel.numconteo.value = getString(R.string.etiqueta_numero) + (activity as MainActivity).sesionAplicacion?.numConteo.toString()
        correccionViewModel.usuario.value = getString(R.string.etiqueta_usuario) + (activity as MainActivity).sesionAplicacion?.empleado?.empCodigo.toString() + " " + (activity as MainActivity).sesionAplicacion?.empleado?.empNombreCompleto.toString()


        correccionViewModel.barraAnterior.value = (this.activity as MainActivity).conteoPocketError!!.pocBarra
        correccionViewModel.cantidadAnterior.value = (this.activity as MainActivity).conteoPocketError!!.pocCantidad.toString()
        correccionViewModel.zonaAnterior.value = (this.activity as MainActivity).conteoPocketError!!.pocZona

        correccionViewModel.barra.value = ""
        correccionViewModel.cantidad.value = ""
        correccionViewModel.zona.value = ""

        editZona?.requestFocus()

        refrescarConexion()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        return root
    }




    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP){
            if(v?.id == R.id.editCantidad){
                guardarConteo()
            }
        }
        return false
    }

    fun guardarConteo() {
        if(correccionViewModel?.saltoPorScaneo!!){
            correccionViewModel?.saltoPorScaneo = false
            insertarConteo()
        }else{
            var guardar: Boolean? = validarCampos()
            if(guardar!!) {
                insertarConteo()
            }
        }
    }

    fun cancelar() {
        (this.activity as MainActivity).reemplazarFragment(R.id.nav_error, false)
    }

    private fun insertarConteo() {
        AsyncTaskGuardarCorreccion(this.activity as MainActivity?, this).execute()
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

    fun refrescarError(error: Boolean) {
        if(error) {
            imgError!!.visibility = View.VISIBLE
        }else{
            imgError!!.visibility = View.GONE
        }
    }

    private fun validarCampos(): Boolean? {
        var guardar: Boolean? = true
        if (editZona?.text.isNullOrBlank()) {
            editZona?.error = getString(R.string.ingrese_zona)
            guardar = false
        } else if(!ValidacionZona.validarZona(editZona?.text.toString(), (activity as MainActivity).sesionAplicacion!!)){
            editZona?.error = getString(R.string.formato_incorrecto)
            guardar = false
        }else{
            editZona?.error = null
        }

        if (editBarra?.text.isNullOrBlank()) {
            editBarra?.error = getString(R.string.ingrese_barra)
            guardar = false
        }else if((editBarra?.text.toString().contains(" ") )|| ( (!editBarra?.text.toString().contains("-") ) && (!ValidacionBarra.validarFormatoBarra(editBarra?.text.toString()) || !ValidacionBarra.validarEAN13Barra(editBarra?.text.toString())))
            || (editBarra?.text.toString().contains("-") && !ValidacionBarra.validarCodigoInterno(editBarra?.text.toString())))
        {
            editBarra?.error =  getString(R.string.formato_incorrecto)
            guardar = false
        } else {
            editBarra?.error = null
        }
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = getString(R.string.ingrese_cantidad)
            guardar = false
        }else if(ValidacionCantidad.validarCantidad(correccionViewModel.cantidad.value!!.toLong())){
            editCantidad?.error =  getString(R.string.error_rango)
            editCantidad?.requestFocus()
            guardar = false
        } else {
            editCantidad?.error = null
        }
        return guardar
    }

    fun refrescarPantalla(codigoLeido: String) {
        if(editZona!!.hasFocus()) {
            if(!ValidacionZona.validarZona(codigoLeido, (activity as MainActivity).sesionAplicacion!!)){
                editZona?.error = getString(R.string.formato_incorrecto)
                return
            }else{
                editZona?.error = null
                editZona!!.setText(codigoLeido)
                editBarra?.requestFocus()
            }
        } else if(editBarra!!.hasFocus()) {
            if(codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido) ){
                editBarra?.error =  getString(R.string.formato_incorrecto)
            }else {
                editBarra?.error = null
                editBarra!!.setText(codigoLeido)
                editCantidad?.requestFocus()
            }
        } else if(editCantidad!!.hasFocus()) {
            if(correccionViewModel.cantidad.value.isNullOrBlank()) {
                editCantidad?.error = getString(R.string.ingrese_cantidad)
                editCantidad?.requestFocus()
            }else if(ValidacionCantidad.validarCantidad(correccionViewModel.cantidad.value!!.toLong())){
                editCantidad?.error =  getString(R.string.error_rango)
                editCantidad?.requestFocus()
            }else {
                editCantidad?.error =  null
                if (codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido)) {
                    editBarra?.error = getString(R.string.formato_incorrecto)
                    correccionViewModel.cantidad.value = ""
                    correccionViewModel.barra.value = ""
                    editBarra?.requestFocus()
                } else {
                    correccionViewModel.saltoPorScaneo = true
                    correccionViewModel.barraAnterior.value = correccionViewModel.barra.value
                    correccionViewModel.cantidadAnterior.value = correccionViewModel.cantidad.value
                    (activity as MainActivity).sesionAplicacion?.ultimaBarra = correccionViewModel.barra.value
                    (activity as MainActivity).sesionAplicacion?.ultimaCantidad = correccionViewModel.cantidad.value
                    (activity as MainActivity).sesionAplicacion?.zonaActual = correccionViewModel.zona.value
                    correccionViewModel.barra.value = codigoLeido
                    correccionViewModel.cantidad.value = ""
                    editBarra!!.setText(codigoLeido)
                    editBarra?.error = null
                    editCantidad?.requestFocus()
                    guardarConteo()
                }
            }

        }
    }

    class AsyncTaskGuardarCorreccion(private var activity: MainActivity?, var correccionFragment: CorreccionFragment?) : AsyncTask<String, String, Int>() {

        var conteoEnviado: MutableList<ConteoPendiente>? = null

        override fun doInBackground(vararg p0: String?): Int? {
            val conteo = Conteo(
                barra = correccionFragment?.correccionViewModel?.barra!!.value.toString(),
                zona = correccionFragment?.correccionViewModel?.zona!!.value.toString(),
                cantidad = correccionFragment?.correccionViewModel?.cantidad!!.value.toString().toInt(),
                estado = Constantes.ESTADO_PENDIENTE_CORRECCION,
                cinId = (activity as MainActivity).sesionAplicacion?.cinId,
                binId = (activity as MainActivity).sesionAplicacion?.binId,
                numConteo = (activity as MainActivity).sesionAplicacion?.numConteo,
                usuId = (activity as MainActivity).sesionAplicacion?.usuId,
                fecha = System.currentTimeMillis(),
                pocId = (this.activity as MainActivity).conteoPocketError!!.pocId,
                barraAnterior = (this.activity as MainActivity).conteoPocketError!!.pocBarra
            )
            correccionFragment?.db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            correccionFragment?.conteoDao = correccionFragment?.db?.conteoDao()
            correccionFragment?.conteoDao?.insertarConteo(conteo)

            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            (this.activity as MainActivity).conteoPocketError!!.corregido = "S"
            for (conteo in activity!!.listaConteoHistorico!!) {
                if( (this.activity as MainActivity).conteoPocketError!!.equals(conteo.pocId)){
                   conteo!!.corregido = "S"
                }
            }
            (this.activity as MainActivity).reemplazarFragment(R.id.nav_error, true)

        }
    }
}