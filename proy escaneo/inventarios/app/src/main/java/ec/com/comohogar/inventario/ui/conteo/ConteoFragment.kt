package ec.com.comohogar.inventario.ui.conteo

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
import ec.com.comohogar.inventario.databinding.FragmentConteoBinding
import android.content.Context
import android.view.KeyEvent
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import android.os.AsyncTask



class ConteoFragment : Fragment(), View.OnKeyListener {

    private lateinit var conteoViewModel: ConteoViewModel

    private var editZona: EditText? = null
    private var editBarra: EditText? = null
    private var editCantidad: EditText? = null

    private var textBarraAnterior: TextView? = null
    private var textCantidadAnterior: TextView? = null
    private var textEstado: TextView? = null

    private var buttonGuardar: Button? = null

    private var db: InventarioDatabase? = null
    private var conteoDao: ConteoDao? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
        textEstado = root.findViewById(R.id.textEstado)

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
        return root
    }


     fun refrescarPantalla(codigoLeido: String) {
        Log.i("fragment", "fragment")
         if(editZona!!.hasFocus()) {
             editZona!!.setText(codigoLeido)
             editBarra?.requestFocus()
         } else if(editBarra!!.hasFocus()) {
             editBarra!!.setText(codigoLeido)
             editCantidad?.requestFocus()
         } else if(editCantidad!!.hasFocus()) {
             conteoViewModel.saltoPorScaneo = true
             conteoViewModel.barraAnterior.value = conteoViewModel.barra.value
             conteoViewModel.cantidadAnterior.value = conteoViewModel.cantidad.value
             conteoViewModel.barra.value = codigoLeido
             //conteoViewModel.cantidad.value = ""
             editBarra!!.setText(codigoLeido)
             editCantidad?.requestFocus()
             guardarConteo()
         }
     }

    fun refrescarEstado(estado: String) {
        textEstado?.setText(estado)
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
        var guardar: Boolean? = true
        if (editZona?.text.isNullOrBlank()) {
            editZona?.error = "Ingrese la zona"
            guardar = false
        }else{
            editZona?.error = null
        }
        if (editBarra?.text.isNullOrBlank()) {
            editBarra?.error = "Ingrese la barra"
            guardar = false
        }else{
            editBarra?.error = null
        }
        if (editCantidad?.text.isNullOrBlank()) {
            editCantidad?.error = "Ingrese la cantidad"
            guardar = false
        }else{
            editCantidad?.error = null
        }
        if(guardar!!) {
            //editBarra?.requestFocus()


            AsyncTask.execute {
                // Insert Data
                val conteo =  Conteo(barra = editBarra?.text.toString(),
                    zona = editZona?.text.toString(),
                    cantidad = editCantidad?.text.toString().toInt())
                db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
                conteoDao = db?.conteoDao()
                val insertarConteo = conteoDao?.insertarConteo(conteo)
                val conteos = conteoDao?.getConteos()
            }

            conteoViewModel.guardarConteo()
        }
    }
}