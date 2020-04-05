package ec.com.comohogar.inventario.ui.pendiente

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
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
import ec.com.comohogar.inventario.persistencia.entities.ReconteoLocal
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.util.ProgressDialog

class ConsultaPendienteFragment : Fragment() {

    private lateinit var consultaPendienteViewModel: ConsultaPendienteViewModel

    private var editBarra: EditText? = null
    private var editZona: EditText? = null
    private var buttonBuscar: ImageButton? = null
    var listview: ListView? = null

    private var db: InventarioDatabase? = null
    private var reconteoLocalDao: ReconteoLocalDao? = null
    private var sesionAplicacion: SesionAplicacion? = null

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

        consultaPendienteViewModel.inventario.value = "Inventario: " + sesionAplicacion?.binId.toString()
        consultaPendienteViewModel.conteo.value = " Conteo: " + sesionAplicacion?.cinId.toString()
        consultaPendienteViewModel.numconteo.value = " Número: " + sesionAplicacion?.numConteo.toString()
        consultaPendienteViewModel.usuario.value = " Usuario: " + sesionAplicacion?.empleado?.empId.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

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
        }
    }

    fun cargarDatosPantalla() {
        dialog = ProgressDialog.setProgressDialog(this!!.activity!!, "Recuperando ítems...")
        dialog?.show()
        AsyncTaskCargarPendientes(this.activity as MainActivity?, this, editBarra?.text.toString(), editZona?.text.toString()).execute()
    }


    class AsyncTaskCargarPendientes(private var activity: MainActivity?, var consultaPendienteFragment: ConsultaPendienteFragment?,
                                    var barra: String?, var zona: String?) : AsyncTask<String, String, Int>() {
        var sesionAplicacion: SesionAplicacion? = null
        var conteoPendiente: MutableList<ConteoPendiente>? = null
        var reconteos: List<ReconteoLocal>? = null

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion
            var db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            conteoPendiente = mutableListOf()
            if(sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                    //Bodega
                } else {
                   //Local
                    var reconteoLocalDao = db?.reconteoLocalDao()
                    reconteos  = mutableListOf()
                    if(barra.isNullOrBlank()) {
                        reconteos = reconteoLocalDao?.getReconteoLocalPendiente()
                    }else {
                        reconteos = reconteoLocalDao?.getReconteoLocalPendienteByBarra(barra)
                    }
                    for (reconteo in reconteos!!) {
                        conteoPendiente!!.add(ConteoPendiente(reconteo.barra, "", reconteo.cantidad))
                    }
                }
            }else if(sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
               //Conteo
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            consultaPendienteFragment?.dialog?.cancel()

            if(conteoPendiente?.isEmpty()!!) {
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage("No existen ítems.")
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Información")
                alert.show()
            }
            var conteoPendienteAdapter = ConteoPendienteAdapter(activity?.applicationContext!!, conteoPendiente)
            consultaPendienteFragment?.listview?.adapter  = conteoPendienteAdapter
        }
    }
}
