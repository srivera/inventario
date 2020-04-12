package ec.com.comohogar.inventario.ui.conteo

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
import ec.com.comohogar.inventario.databinding.FragmentConteoBinding
import android.content.Context
import android.view.KeyEvent
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import android.os.AsyncTask
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.validacion.ValidacionBarra
import ec.com.comohogar.inventario.validacion.ValidacionCantidad
import ec.com.comohogar.inventario.validacion.ValidacionZona


class ConteoFragment : Fragment(), View.OnKeyListener {

    private lateinit var conteoViewModel: ConteoViewModel

    private var sesionAplicacion: SesionAplicacion? = null

    private var editZona: EditText? = null
    private var editBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var textBarraAnterior: TextView? = null
    private var textCantidadAnterior: TextView? = null

    private var buttonGuardar: Button? = null

    private var db: InventarioDatabase? = null
    private var conteoDao: ConteoDao? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        val binding: FragmentConteoBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_conteo, container, false
        )
        binding.setLifecycleOwner(this)
        conteoViewModel = ViewModelProviders.of(this).get(ConteoViewModel::class.java)
        binding.uiController = this
        binding.conteoViewModel = conteoViewModel
        val root = binding.getRoot()

        editZona = root.findViewById(R.id.editZona)
        editBarra = root.findViewById(R.id.editBarra)
        editCantidad = root.findViewById(R.id.editCantidad)

        textBarraAnterior = root.findViewById(R.id.textBarraAnterior)
        textCantidadAnterior = root.findViewById(R.id.textCantidadAnterior)

        buttonGuardar = root.findViewById(R.id.buttonGuardar)

        editCantidad?.setOnKeyListener(this)

        editCantidad?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm!!.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
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
                conteoViewModel.zona.value = ""
                val imm = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

        }

        conteoViewModel.inventario.value = "Inventario: " + sesionAplicacion?.binId.toString()
        conteoViewModel.conteo.value = " Conteo: " + sesionAplicacion?.cinId.toString()
        conteoViewModel.numconteo.value = " Número: " + sesionAplicacion?.numConteo.toString()
        conteoViewModel.usuario.value = " Usuario: " + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        return root
    }


     fun refrescarPantalla(codigoLeido: String) {
         if(editZona!!.hasFocus()) {
             if(!ValidacionZona.validarZona(codigoLeido, sesionAplicacion!!)){
                 editZona?.error = "Formato incorrecto"
                 return
             }else{
                 editZona?.error = null
                 editZona!!.setText(codigoLeido)
                 editBarra?.requestFocus()
             }
         } else if(editBarra!!.hasFocus()) {
             if(codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido) ){
                 editBarra?.error =  "Formato incorrecto"
             }else {
                 editBarra?.error = null
                 editBarra!!.setText(codigoLeido)
                 editCantidad?.requestFocus()
             }
         } else if(editCantidad!!.hasFocus()) {
             if(conteoViewModel.cantidad.value.isNullOrBlank()) {
                 editCantidad?.error = "Ingrese la cantidad"
                 editCantidad?.requestFocus()
             }else if(ValidacionCantidad.validarCantidad(conteoViewModel.cantidad.value!!.toInt())){
                 editCantidad?.error =  "Cantidad fuera de rango"
                 editCantidad?.requestFocus()
             }else {
                 editCantidad?.error =  null
                 if (codigoLeido.contains(" ") || !ValidacionBarra.validarFormatoBarra(codigoLeido) || !ValidacionBarra.validarEAN13Barra(codigoLeido)) {
                     editBarra?.error = "Formato incorrecto"
                     conteoViewModel.cantidad.value = ""
                     conteoViewModel.barra.value = ""
                     editBarra?.requestFocus()
                 } else {
                     conteoViewModel.saltoPorScaneo = true
                     conteoViewModel.barraAnterior.value = conteoViewModel.barra.value
                     conteoViewModel.cantidadAnterior.value = conteoViewModel.cantidad.value
                     conteoViewModel.barra.value = codigoLeido
                     conteoViewModel.cantidad.value = ""
                     editBarra!!.setText(codigoLeido)
                     editBarra?.error = null
                     editCantidad?.requestFocus()
                     guardarConteo()
                 }
             }

         }
     }

    fun refrescarEstado(estado: String) {
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
        if(conteoViewModel?.saltoPorScaneo!!){
            conteoViewModel?.saltoPorScaneo = false
            insertarConteo()
        }else{
            var guardar: Boolean? = validarCampos()
            if(guardar!!) {
                conteoViewModel?.barraAnterior.value = conteoViewModel?.barra.value.toString()
                conteoViewModel?.cantidadAnterior.value =
                    conteoViewModel?.cantidad.value.toString()
                conteoViewModel.barra.value = ""
                conteoViewModel.cantidad.value = ""
                editBarra?.requestFocus()
                insertarConteo()
            }
        }
    }

    private fun insertarConteo() {
        AsyncTask.execute {
            val conteo = Conteo(
                barra = conteoViewModel.barraAnterior.value.toString(),
                zona = editZona?.text.toString(),
                cantidad = conteoViewModel.cantidadAnterior.value.toString().toInt(),
                estado = Constantes.ESTADO_PENDIENTE,
                cinId = sesionAplicacion?.cinId,
                binId = sesionAplicacion?.binId,
                numConteo = sesionAplicacion?.numConteo,
                usuId = sesionAplicacion?.usuId
            )
            db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            conteoDao = db?.conteoDao()
            conteoDao?.insertarConteo(conteo)
        }
    }


    private fun validarCampos(): Boolean? {
        var guardar: Boolean? = true
        if (editZona?.text.isNullOrBlank()) {
            editZona?.error = "Ingrese la zona"
            guardar = false
        } else if(!ValidacionZona.validarZona(editZona?.text.toString(), sesionAplicacion!!)){
            editZona?.error = "Formato incorrecto"
            guardar = false
        }else{
            editZona?.error = null
        }

        if (editBarra?.text.isNullOrBlank()) {
            editBarra?.error = "Ingrese la barra"
            guardar = false
        }else if(editBarra?.text.toString().contains(" ") || !ValidacionBarra.validarFormatoBarra(editBarra?.text.toString()) || !ValidacionBarra.validarEAN13Barra(editBarra?.text.toString()) ){
            editBarra?.error =  "Formato incorrecto"
            guardar = false
        } else {
            editBarra?.error = null
        }
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = "Ingrese la cantidad"
            guardar = false
        }else if(ValidacionCantidad.validarCantidad(conteoViewModel.cantidad.value!!.toInt())){
            editCantidad?.error =  "Cantidad fuera de rango"
            editCantidad?.requestFocus()
            guardar = false
        } else {
            editCantidad?.error = null
        }
        return guardar
    }
}