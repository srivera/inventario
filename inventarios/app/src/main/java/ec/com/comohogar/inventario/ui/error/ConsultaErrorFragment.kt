package ec.com.comohogar.inventario.ui.error

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
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
import ec.com.comohogar.inventario.modelo.ConteoPocketError
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call



class ConsultaErrorFragment : Fragment() {

    private lateinit var consultaErrorViewModel: ConsultaErrorViewModel

    var listview: ListView? = null

    private var sesionAplicacion: SesionAplicacion? = null

    private var imgError: ImageView? = null
    private var imgConexion: ImageView? = null

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
        imgError = root.findViewById(R.id.imgError)
        imgConexion = root.findViewById(R.id.imgConexion)

        dialog = ProgressDialog.setProgressDialog(this!!.requireActivity(), getString(R.string.recuperar_items))
        dialog?.show()

        consultaErrorViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        consultaErrorViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        consultaErrorViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        consultaErrorViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        recuperarReconteo()

        listview!!.setOnItemClickListener { parent, _, position, _ ->

           if((activity as MainActivity).listaConteoHistorico?.get(position)!!.corregido == null ) {
               (this.activity as MainActivity).conteoPocketError = (activity as MainActivity).listaConteoHistorico?.get(position)
               (this.activity as MainActivity).reemplazarFragment(R.id.nav_correccion, false)
           }

        }

        refrescarConexion()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        return root
    }

    private fun recuperarReconteo() {
        AsyncTaskConsultarError(this.activity as MainActivity?, this).execute()
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

    fun refrescarLista(actualizar: Boolean) {
        if(actualizar)
          (listview?.adapter as BaseAdapter).notifyDataSetChanged()
    }

    class AsyncTaskConsultarError(private var activity: MainActivity?, var consultaErrorFragment: ConsultaErrorFragment?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null

        var conteosCorreccion:  List<Conteo>? = null

        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(activity as MainActivity)

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            val call: Call<List<ConteoPocketError>> = ApiClient.getClient.consultarErrores(
                sesionAplicacion?.empleado!!.usuId, sesionAplicacion?.binId!!, sesionAplicacion?.numConteo!!.toLong())
            try {
                val response = call.execute()
                activity?.listaConteoHistorico = response.body()
                var conteoDao = db?.conteoDao()
                conteosCorreccion = conteoDao?.getConteoPendienteCorreccion()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }


            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaErrorFragment?.dialog?.cancel()
            if(activity?.listaConteoHistorico == null) {
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(activity?.getString(R.string.no_items))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
                var conteoPendienteAdapter = ConteoErrorAdapter(activity?.applicationContext!!, null)
                consultaErrorFragment?.listview?.adapter  = conteoPendienteAdapter
            }else if(!activity?.listaConteoHistorico?.isEmpty()!!) {
                if(conteosCorreccion != null) {
                    for (conteoError in activity?.listaConteoHistorico!!) {
                        for (conteo in conteosCorreccion!!) {
                            if (conteo.pocId?.equals(conteoError.pocId)!! && conteo.barraAnterior?.equals(
                                    conteoError.pocBarra
                                )!!
                            ) {
                                conteoError.corregido = "S"
                            }
                        }
                    }
                }

                var conteoPendienteAdapter = ConteoErrorAdapter(activity?.applicationContext!!, activity?.listaConteoHistorico)
                consultaErrorFragment?.listview?.adapter  = conteoPendienteAdapter




            }
        }
    }
}
