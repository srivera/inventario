package ec.com.comohogar.inventario.ui.consultaconteo

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.adapter.ReconteoItemAdapter
import ec.com.comohogar.inventario.databinding.FragmentConsultaConteoUsuarioBinding
import ec.com.comohogar.inventario.modelo.ReconteoItemSeccion
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import ec.com.comohogar.inventario.util.Paginator



class ConsultaConteoUsuarioFragment : Fragment() {

    private lateinit var consultaConteoUsuarioViewModel: ConsultaConteoUsuarioViewModel

    var listview: ListView? = null

    private var nextBtn: Button? = null
    private var prevBtn: Button? = null

    var p = Paginator()
    private val totalPages = p.totalPages
    var currentPage = 0

    var reconteoItemAdapter: ReconteoItemAdapter? = null

    private var sesionAplicacion: SesionAplicacion? = null

    var listaItems: ArrayList<ReconteoItemSeccion>? = null

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentConsultaConteoUsuarioBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_consulta_conteo_usuario, container, false
        )
        binding.setLifecycleOwner(this)
        consultaConteoUsuarioViewModel = ViewModelProviders.of(this).get(ConsultaConteoUsuarioViewModel::class.java)
        binding.uiController = this
        binding.consultaConteoUsuarioViewModel = consultaConteoUsuarioViewModel
        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        listview = root.findViewById(R.id.listview)
        nextBtn = root.findViewById(R.id.btnNext)
        prevBtn = root.findViewById(R.id.btnPrev)

        recuperarReconteo()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        return root
    }

    fun siguiente() {
        currentPage += 1
        bindData(currentPage)
        toggleButtons()
    }

    fun anterior() {
        currentPage -= 1
        bindData(currentPage)
        toggleButtons()
    }

    fun recuperarReconteo() {
        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, getString(R.string.recuperar_items))
        dialog?.show()
        AsyncTaskConsultarHistorico(this.activity as MainActivity?, this).execute()
    }

    fun refrescarError(error: Boolean) {

    }

    private fun toggleButtons() {

        if (totalPages <= 1) {
            nextBtn?.setEnabled(false)
            prevBtn?.setEnabled(false)
        } else if (currentPage === totalPages) {
            nextBtn?.setEnabled(false)
            prevBtn?.setEnabled(true)
        } else if (currentPage == 0) {
            prevBtn?.setEnabled(false)
            nextBtn?.setEnabled(true)
        } else if (currentPage >= 1 && currentPage <= totalPages) {
            nextBtn?.setEnabled(true)
            prevBtn?.setEnabled(true)
        }
    }

    private fun bindData(page: Int) {
        reconteoItemAdapter = ReconteoItemAdapter(activity!!.applicationContext, p.getCurrentConteos(page, listaItems!!.size, listaItems))
        listview?.setAdapter(reconteoItemAdapter)
    }


    class AsyncTaskConsultarHistorico(private var activity: MainActivity?, var consultaConteoUsuarioFragment: ConsultaConteoUsuarioFragment?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null


        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            var esReconteo = "N"
            if (sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                esReconteo = "S"
            }
            val call: Call<List<ReconteoItemSeccion>> = ApiClient.getClient.consultarConteoInventarioUsuario(
                (activity as MainActivity).idUsuarioConsulta!!,sesionAplicacion?.binId!!,
                sesionAplicacion?.numConteo!!.toLong(),esReconteo)
            try {
                var response = call.execute()
                consultaConteoUsuarioFragment?.listaItems = response.body() as ArrayList<ReconteoItemSeccion>?

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaConteoUsuarioFragment?.dialog?.cancel()
            if(consultaConteoUsuarioFragment?.listaItems == null) {
                var reconteoUsuarioAdapter = ReconteoItemAdapter(activity?.applicationContext!!, consultaConteoUsuarioFragment?.listaItems)
                consultaConteoUsuarioFragment?.listview?.adapter = reconteoUsuarioAdapter

                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(activity?.getString(R.string.no_items))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
            }else if(!consultaConteoUsuarioFragment?.listaItems?.isEmpty()!!) {
                consultaConteoUsuarioFragment?.bindData(0)
            }
        }
    }
}
