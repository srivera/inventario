package ec.com.comohogar.inventario.ui.error

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.adapter.ConteoErrorAdapter
import ec.com.comohogar.inventario.databinding.FragmentErrorBinding
import ec.com.comohogar.inventario.modelo.ConteoPendiente
import ec.com.comohogar.inventario.modelo.ConteoPocketError
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call

class ConsultaErrorFragment : Fragment() {

    private lateinit var consultaErrorViewModel: ConsultaErrorViewModel

    var listview: ListView? = null

    private var sesionAplicacion: SesionAplicacion? = null

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentErrorBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_error, container, false
        )
        binding.setLifecycleOwner(this)
        consultaErrorViewModel = ViewModelProviders.of(this).get(ConsultaErrorViewModel::class.java)
        binding.uiController = this
        binding.consultaErrorViewModel = consultaErrorViewModel
        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        listview = root.findViewById(R.id.listview)

        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, getString(R.string.recuperar_items))
        dialog?.show()

        consultaErrorViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        consultaErrorViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        consultaErrorViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        consultaErrorViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        recuperarReconteo()
        return root
    }

    private fun recuperarReconteo() {
        AsyncTaskConsultarError(this.activity as MainActivity?, this).execute()
    }

    class AsyncTaskConsultarError(private var activity: MainActivity?, var consultaErrorFragment: ConsultaErrorFragment?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null
        var conteoEnviado: MutableList<ConteoPendiente>? = null

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            val call: Call<List<ConteoPocketError>> = ApiClient.getClient.consultarErrores(
                sesionAplicacion?.usuId!!, sesionAplicacion?.binId!!, sesionAplicacion?.numConteo!!.toLong())
            try {
                val response = call.execute()
                val listaConteoHistorico = response.body()
                conteoEnviado = mutableListOf()
                for (conteo in listaConteoHistorico!!) {
                    conteoEnviado!!.add(ConteoPendiente(conteo.pocBarra, conteo.pocZona, conteo.pocCantidad, conteo.pocObservacion))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaErrorFragment?.dialog?.cancel()
            if(conteoEnviado == null) {
                conteoEnviado = mutableListOf()
                var conteoPendienteAdapter = ConteoErrorAdapter(activity?.applicationContext!!, conteoEnviado)
                consultaErrorFragment?.listview?.adapter  = conteoPendienteAdapter
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
                var conteoPendienteAdapter = ConteoErrorAdapter(activity?.applicationContext!!, conteoEnviado)
                consultaErrorFragment?.listview?.adapter  = conteoPendienteAdapter

            }

        }
    }
}
