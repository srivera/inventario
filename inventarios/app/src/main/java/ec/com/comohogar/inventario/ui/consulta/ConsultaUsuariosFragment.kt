package ec.com.comohogar.inventario.ui.consulta

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.adapter.ReconteoUsuarioAdapter
import ec.com.comohogar.inventario.databinding.FragmentConsultaUsuarioBinding
import ec.com.comohogar.inventario.modelo.ReconteoAsignado
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call

class ConsultaUsuariosFragment : Fragment() {

    private lateinit var consultaUsuariosViewModel: ConsultaUsuariosViewModel

    var listview: ListView? = null

    private var imgError: ImageView? = null

    private var sesionAplicacion: SesionAplicacion? = null

    var listaUsuariosAsignado: ArrayList<ReconteoAsignado>? = null

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentConsultaUsuarioBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_consulta_usuario, container, false
        )
        binding.setLifecycleOwner(this)
        consultaUsuariosViewModel = ViewModelProviders.of(this).get(ConsultaUsuariosViewModel::class.java)
        binding.uiController = this
        binding.consultaUsuariosViewModel = consultaUsuariosViewModel
        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        listview = root.findViewById(R.id.listview)
        imgError = root.findViewById(R.id.imgError)

        listview!!.setOnItemClickListener { parent, _, position, _ ->
            (this.activity as MainActivity).idUsuarioConsulta = listaUsuariosAsignado?.get(position)?.usuId
            (this.activity as MainActivity).reemplazarFragment(R.id.nav_consulta_conteo_usuario, false)
        }

        consultaUsuariosViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        consultaUsuariosViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        consultaUsuariosViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        consultaUsuariosViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        recuperarReconteo()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        return root
    }


    fun recuperarReconteo() {
        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, getString(R.string.recuperar_items))
        dialog?.show()
        AsyncTaskConsultarHistorico(this.activity as MainActivity?, this).execute()
    }

    fun refrescarError(error: Boolean) {
        if(error) {
            imgError!!.visibility = View.VISIBLE
        }else{
            imgError!!.visibility = View.GONE
        }
    }

    class AsyncTaskConsultarHistorico(private var activity: MainActivity?, var consultaUsuariosFragment: ConsultaUsuariosFragment?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null


        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            val call: Call<List<ReconteoAsignado>> = ApiClient.getClient.consultarConteoUsuarioTotal(
                sesionAplicacion?.binId!!, sesionAplicacion?.numConteo!!.toLong())
            try {
                var response = call.execute()
                consultaUsuariosFragment?.listaUsuariosAsignado = response.body() as ArrayList<ReconteoAsignado>?

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaUsuariosFragment?.dialog?.cancel()
            if(consultaUsuariosFragment?.listaUsuariosAsignado == null) {
                var reconteoUsuarioAdapter = ReconteoUsuarioAdapter(activity?.applicationContext!!, consultaUsuariosFragment?.listaUsuariosAsignado)
                consultaUsuariosFragment?.listview?.adapter = reconteoUsuarioAdapter

                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(activity?.getString(R.string.no_items))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
            }else if(!consultaUsuariosFragment?.listaUsuariosAsignado?.isEmpty()!!) {
                var reconteoUsuarioAdapter =
                    ReconteoUsuarioAdapter(activity?.applicationContext!!, consultaUsuariosFragment?.listaUsuariosAsignado)
                consultaUsuariosFragment?.listview?.adapter = reconteoUsuarioAdapter
            }
        }
    }
}
