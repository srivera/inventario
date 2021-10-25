package ec.com.comohogar.inventario.ui.enviado

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.adapter.ConteoPendienteAdapter
import ec.com.comohogar.inventario.databinding.FragmentEnviadoBinding
import ec.com.comohogar.inventario.modelo.ConteoPendiente
import ec.com.comohogar.inventario.modelo.ConteoPocketHistorico
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call

class ConsultaEnviadoFragment : Fragment() {

    private lateinit var consultaEnviadoViewModel: ConsultaEnviadoViewModel

    private var editBarra: EditText? = null
    private var editZona: EditText? = null
    private var buttonBuscar: ImageButton? = null
    var listview: ListView? = null

    private var imgError: ImageView? = null

    private var sesionAplicacion: SesionAplicacion? = null

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentEnviadoBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_enviado, container, false
        )
        binding.setLifecycleOwner(this)
        consultaEnviadoViewModel = ViewModelProviders.of(this).get(ConsultaEnviadoViewModel::class.java)
        binding.uiController = this
        binding.consultaEnviadoViewModel = consultaEnviadoViewModel
        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        editBarra = root.findViewById(R.id.editBarra)
        editZona = root.findViewById(R.id.editZona)
        buttonBuscar = root.findViewById(R.id.buttonGuardar)
        listview = root.findViewById(R.id.listview)
        imgError = root.findViewById(R.id.imgError)

        editZona?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                consultaEnviadoViewModel.zona.value = ""
            }
        }

        editBarra?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                consultaEnviadoViewModel.barra.value = ""
            }
        }

        consultaEnviadoViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        consultaEnviadoViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        consultaEnviadoViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        consultaEnviadoViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        recuperarReconteo()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        return root
    }

    fun refrescarPantalla(codigoLeido: String) {
        if (editBarra!!.hasFocus()) {
            editBarra!!.setText(codigoLeido)
            editZona?.requestFocus()
            recuperarReconteo()
        } else if (editZona!!.hasFocus()) {
            editZona!!.setText(codigoLeido)
            recuperarReconteo()
        }
    }

    fun recuperarReconteo() {
        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, getString(R.string.recuperar_items))
        dialog?.show()
        AsyncTaskConsultarHistorico(this.activity as MainActivity?, this, editBarra?.text.toString(), editZona?.text.toString()).execute()
    }

    fun refrescarError(error: Boolean) {
        if(error) {
            imgError!!.visibility = View.VISIBLE
        }else{
            imgError!!.visibility = View.GONE
        }
    }

    class AsyncTaskConsultarHistorico(private var activity: MainActivity?, var consultaEnviadoFragment: ConsultaEnviadoFragment?,
            var barra: String?, var zona: String?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null
        var conteoEnviado: MutableList<ConteoPendiente>? = null


        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            val call: Call<List<ConteoPocketHistorico>> = ApiClient.getClient.consultarHistorico(
                sesionAplicacion?.usuId!!,  sesionAplicacion?.numConteo!!, sesionAplicacion?.binId!!,
                barra!!, zona!!)
            try {
                var response = call.execute()
                val listaConteoHistorico = response.body()
                conteoEnviado = mutableListOf()

                for (conteo in listaConteoHistorico!!) {
                    conteoEnviado!!.add(ConteoPendiente(conteo.barra, conteo.zona, conteo.cantidad, ""))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaEnviadoFragment?.dialog?.cancel()
            if(conteoEnviado == null) {
                conteoEnviado = mutableListOf()
                var conteoPendienteAdapter =
                    ConteoPendienteAdapter(activity?.applicationContext!!, conteoEnviado)
                consultaEnviadoFragment?.listview?.adapter = conteoPendienteAdapter

                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(activity?.getString(R.string.no_items))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
            }else if(!conteoEnviado?.isEmpty()!!) {
                var conteoPendienteAdapter =
                    ConteoPendienteAdapter(activity?.applicationContext!!, conteoEnviado)
                consultaEnviadoFragment?.listview?.adapter = conteoPendienteAdapter
            }
        }
    }
}
