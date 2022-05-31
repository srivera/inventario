package ec.com.comohogar.inventario.ui.pendiente

import android.content.DialogInterface
import android.content.SharedPreferences
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
import ec.com.comohogar.inventario.databinding.FragmentPendienteBinding
import ec.com.comohogar.inventario.modelo.ConteoPendiente
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.dao.ReconteoLocalDao
import ec.com.comohogar.inventario.persistencia.entities.Conteo
import ec.com.comohogar.inventario.persistencia.entities.ReconteoBodega
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.util.ProgressDialog
import ec.com.comohogar.inventario.webservice.ApiClient

class ConsultaPendienteFragment : Fragment() {

    private lateinit var consultaPendienteViewModel: ConsultaPendienteViewModel

    private var editBarra: EditText? = null
    private var editZona: EditText? = null
    private var buttonBuscar: ImageButton? = null
    var listview: ListView? = null

    private var db: InventarioDatabase? = null
    private var reconteoLocalDao: ReconteoLocalDao? = null
    private var sesionAplicacion: SesionAplicacion? = null

    private var imgError: ImageView? = null
    private var imgConexion: ImageView? = null

    var dialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentPendienteBinding
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_pendiente, container, false
        )
        binding.setLifecycleOwner(this)
        consultaPendienteViewModel = ViewModelProviders.of(this).get(ConsultaPendienteViewModel::class.java)
        binding.uiController = this
        binding.consultaPendienteViewModel = consultaPendienteViewModel
        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
        reconteoLocalDao = db?.reconteoLocalDao()

        editBarra = root.findViewById(R.id.editBarra)
        editZona = root.findViewById(R.id.editZona)
        buttonBuscar = root.findViewById(R.id.buttonGuardar)
        listview = root.findViewById(R.id.listview)
        imgError = root.findViewById(R.id.imgError)
        imgConexion = root.findViewById(R.id.imgConexion)

        consultaPendienteViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        consultaPendienteViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        consultaPendienteViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        consultaPendienteViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        editZona?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                consultaPendienteViewModel.zona.value = ""
            }
        }

        editBarra?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                consultaPendienteViewModel.barra.value = ""
            }
        }

        refrescarConexion()

        (activity as MainActivity)?.errorPendiente?.let { refrescarError(it) }
        cargarDatosPantalla()
        return root
    }

    fun refrescarPantalla(codigoLeido: String) {
        if (editBarra!!.hasFocus()) {
            editBarra!!.setText(codigoLeido)
            editZona?.requestFocus()
            cargarDatosPantalla()
        } else if (editZona!!.hasFocus()) {
            editZona!!.setText(codigoLeido)
            cargarDatosPantalla()
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

    fun refrescarError(error: Boolean) {
        if(error) {
            imgError!!.visibility = View.VISIBLE
        }else{
            imgError!!.visibility = View.GONE
        }
    }

    fun cargarDatosPantalla() {
        dialog = ProgressDialog.setProgressDialog(this!!.requireActivity(), getString(R.string.recuperar_items))
        dialog?.show()
        AsyncTaskCargarPendientes(this.activity as MainActivity?, this, editBarra?.text.toString(), editZona?.text.toString()).execute()
    }


    class AsyncTaskCargarPendientes(private var activity: MainActivity?, var consultaPendienteFragment: ConsultaPendienteFragment?,
                                    var barra: String?, var zona: String?) : AsyncTask<String, String, Int>() {
        var sesionAplicacion: SesionAplicacion? = null
        var conteoPendiente: MutableList<ConteoPendiente>? = null


        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion
            var db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            conteoPendiente = mutableListOf()
            var conteoDao = db?.conteoDao()
            if(sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                    //Bodega
                    var conteos: List<Conteo>? = null
                    conteos  = mutableListOf()
                    if(barra.isNullOrBlank() && zona.isNullOrBlank() ) {
                        conteos = conteoDao?.getConteoPendienteTodos()
                    }else if(!barra.isNullOrBlank()){
                        conteos = conteoDao?.getConteoPendienteByBarra(barra)
                    }else if(!zona.isNullOrBlank()){
                        conteos = conteoDao?.getConteoPendienteByZona(zona)
                    }else{
                        conteos = conteoDao?.getConteoPendienteByBarraAndZona(barra, zona)
                    }

                    var reconteoBodegaDao = db?.reconteoBodegaDao()
                    var reconteos: List<ReconteoBodega>? = null
                    reconteos  = mutableListOf()
                    if(barra.isNullOrBlank() && zona.isNullOrBlank() ) {
                        reconteos = reconteoBodegaDao?.getReconteoBodegaPendiente()
                    }else if(!barra.isNullOrBlank()){
                        reconteos = reconteoBodegaDao?.getReconteoPendienteByBarra(barra)
                    }else if(!zona.isNullOrBlank()){
                        reconteos = reconteoBodegaDao?.getReconteoPendienteByZona(zona)
                    }else{
                        reconteos = reconteoBodegaDao?.getReconteoPendienteByBarraAndZona(barra, zona)
                    }

                    for (reconteo in reconteos!!) {
                        conteoPendiente!!.add(ConteoPendiente(reconteo.barra, reconteo.rcoUbicacion, reconteo.cantidad, ""))
                    }
                    for (conteo in conteos!!) {
                        conteoPendiente!!.add(ConteoPendiente(conteo.barra, conteo.zona, conteo.cantidad, ""))
                    }
                } else {
                   //Local
                    var conteos: List<Conteo>? = null
                    conteos  = mutableListOf()
                    if(barra.isNullOrBlank() && zona.isNullOrBlank() ) {
                        conteos = conteoDao?.getConteoPendienteTodos()
                    }else if(!barra.isNullOrBlank()){
                        conteos = conteoDao?.getConteoPendienteByBarra(barra)
                    }else if(!zona.isNullOrBlank()){
                        conteos = conteoDao?.getConteoPendienteByZona(zona)
                    }else{
                        conteos = conteoDao?.getConteoPendienteByBarraAndZona(barra, zona)
                    }
                    for (conteo in conteos!!) {
                        conteoPendiente!!.add(ConteoPendiente(conteo.barra, "", conteo.cantidad, ""))
                    }
                }
            }else if(sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
               //Conteo

                var conteos: List<Conteo>? = null
                conteos  = mutableListOf()
                if(barra.isNullOrBlank() && zona.isNullOrBlank() ) {
                    conteos = conteoDao?.getConteoPendienteTodos()
                }else if(!barra.isNullOrBlank()){
                    conteos = conteoDao?.getConteoPendienteByBarra(barra)
                }else if(!zona.isNullOrBlank()){
                    conteos = conteoDao?.getConteoPendienteByZona(zona)
                }else{
                    conteos = conteoDao?.getConteoPendienteByBarraAndZona(barra, zona)
                }
                for (conteo in conteos!!) {
                    conteoPendiente!!.add(ConteoPendiente(conteo.barra, conteo.zona, conteo.cantidad, ""))
                }
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaPendienteFragment?.dialog?.cancel()

            if(conteoPendiente?.isEmpty()!!) {
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(activity?.getString(R.string.no_items))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
            }
            var conteoPendienteAdapter = ConteoPendienteAdapter(activity?.applicationContext!!, conteoPendiente)
            consultaPendienteFragment?.listview?.adapter  = conteoPendienteAdapter
        }
    }
}
