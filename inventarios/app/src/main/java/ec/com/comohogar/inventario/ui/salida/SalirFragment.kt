package ec.com.comohogar.inventario.ui.salida

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.databinding.FragmentSalirBinding
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.persistencia.dao.ConteoDao
import ec.com.comohogar.inventario.persistencia.dao.ReconteoBodegaDao
import ec.com.comohogar.inventario.persistencia.dao.ReconteoLocalDao
import ec.com.comohogar.inventario.util.Constantes

class SalirFragment : Fragment() {

    private lateinit var salirViewModel: SalirViewModel

    private var db: InventarioDatabase? = null
    private var reconteoBodegaDao: ReconteoBodegaDao? = null
    private var reconteoLocalDao: ReconteoLocalDao? = null
    private var conteoDao: ConteoDao? = null

    private var sesionAplicacion: SesionAplicacion? = null

    var buttonSalir: Button? = null

    private var textTotalValor: TextView? = null
    private var textEnviadoValor: TextView? = null
    private var textPendienteValor: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentSalirBinding

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_salir, container, false
        )

        binding.setLifecycleOwner(this)

        salirViewModel = ViewModelProviders.of(this).get(SalirViewModel::class.java)

        binding.uiController = this

        binding.salirViewModel = salirViewModel

        val root = binding.getRoot()

        sesionAplicacion = activity?.applicationContext as SesionAplicacion?

        buttonSalir = root.findViewById(R.id.buttonSalir)
        textTotalValor = root.findViewById(R.id.textTotalValor)
        textEnviadoValor = root.findViewById(R.id.textEnviadoValor)
        textPendienteValor = root.findViewById(R.id.textPendienteValor)

        db = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
        reconteoBodegaDao = db?.reconteoBodegaDao()
        salirViewModel.inventario.value = "Inventario: " + sesionAplicacion?.binId.toString()
        salirViewModel.conteo.value = " Conteo: " + sesionAplicacion?.cinId.toString()
        salirViewModel.numconteo.value = " Número: " + sesionAplicacion?.numConteo.toString()
        salirViewModel.usuario.value = " Usuario: " + sesionAplicacion?.empleado?.empId.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        cargarDatosPantalla()

        return root
    }

    fun salir() {
        val inventarioPreferences: SharedPreferences = activity!!.getSharedPreferences(Constantes.PREF_NAME, 0)
        inventarioPreferences.edit().clear().commit()
        activity!!.finish()
    }

   private fun cargarDatosPantalla() {
        AsyncTaskCargarDatosReconteo(this.activity as MainActivity?, this).execute()

    }

    class AsyncTaskCargarDatosReconteo(private var activity: MainActivity?, var salirFragment: SalirFragment) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null
        var total: Int? = null
        var totalEnviado: Int? = null
        var totalPendiente: Int? = null

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)

            if(sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                    //Bodega
                    var reconteoBodegaDao = db?.reconteoBodegaDao()
                    var conteoDao = db?.conteoDao()
                    total = reconteoBodegaDao?.count()
                    totalEnviado = reconteoBodegaDao?.countEnviado()?.plus(conteoDao?.countEnviado()!!)
                    totalPendiente = reconteoBodegaDao?.countPendiente()?.plus(conteoDao?.countPendiente()!!)
                } else {
                    //Local
                    var reconteoLocalDao = db?.reconteoLocalDao()
                    total = reconteoLocalDao?.count()
                    totalEnviado = reconteoLocalDao?.countEnviado()
                    totalPendiente = reconteoLocalDao?.countPendiente()
                }
            }else if(sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
                //Conteo
                var conteoDao = db?.conteoDao()
                total = conteoDao?.count()
                totalEnviado = conteoDao?.countEnviado()
                totalPendiente = conteoDao?.countPendiente()
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            salirFragment?.salirViewModel?.total.value = total.toString()
            salirFragment?.salirViewModel?.totalEnviado.value = totalEnviado.toString()
            salirFragment?.salirViewModel?.totalPendiente.value = totalPendiente.toString()
            if(totalPendiente?.compareTo(0)!! > 0){
                salirFragment?.buttonSalir?.setEnabled(false)
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage("Existen ítems pendientes de enviar. Revise el wifi de su dispositivo para enviar los ítems y vuelva a intentar salir")
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Información")
                alert.show()
            }
        }
    }


}