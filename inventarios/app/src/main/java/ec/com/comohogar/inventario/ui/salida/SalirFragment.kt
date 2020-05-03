package ec.com.comohogar.inventario.ui.salida

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
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
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        salirViewModel.inventario.value = getString(R.string.etiqueta_inventario) + sesionAplicacion?.binId.toString()
        salirViewModel.conteo.value = getString(R.string.etiqueta_conteo) + sesionAplicacion?.cinId.toString()
        salirViewModel.numconteo.value = getString(R.string.etiqueta_numero)+ sesionAplicacion?.numConteo.toString()
        salirViewModel.usuario.value = getString(R.string.etiqueta_usuario) + sesionAplicacion?.empleado?.empCodigo.toString() + " " + sesionAplicacion?.empleado?.empNombreCompleto.toString()

        cargarDatosPantalla()

        return root
    }

    fun salir() {
        val dialogBuilder = AlertDialog.Builder(activity!!)
        dialogBuilder.setMessage(getString(R.string.salir_inventario))
            .setCancelable(false)
            .setPositiveButton("Si", DialogInterface.OnClickListener { dialog, id ->
                if(sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                    cerrarReconteo()
                }else if(sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
                    AsyncTaskEliminarDatos(this@SalirFragment.activity as MainActivity?).execute()
                }
            }).setNegativeButton("No", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
            })

        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.confirmacion))
        alert.show()
    }

    private fun cerrarReconteo() {
        val call: Call<Long> = ApiClient.getClient.cerrarUsuarioConteo(
            sesionAplicacion?.usuId!!,
            sesionAplicacion?.binId!!
        )
        call.enqueue(object : Callback<Long> {

            override fun onResponse(call: Call<Long>?, response: Response<Long>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                AsyncTaskEliminarDatos(this@SalirFragment.activity as MainActivity?).execute()
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                Log.i("error", "error")
                val dialogBuilder = AlertDialog.Builder(activity!!)
                dialogBuilder.setMessage(getString(R.string.error_wifi_salir))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
            }

        })
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
            var conteoDao = db?.conteoDao()
            if(sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                    //Bodega
                    var reconteoBodegaDao = db?.reconteoBodegaDao()
                    total = reconteoBodegaDao?.count()
                    totalEnviado = reconteoBodegaDao?.countEnviado()?.plus(conteoDao?.countEnviado()!!)
                    totalPendiente = reconteoBodegaDao?.countPendiente()?.plus(conteoDao?.countPendiente()!!)
                } else {
                    //Local
                    var reconteoLocalDao = db?.reconteoLocalDao()
                    total = reconteoLocalDao?.count()
                    totalEnviado = conteoDao?.countEnviado()
                    totalPendiente = conteoDao?.countPendiente()
                }
            }else if(sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
                //Conteo
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
                dialogBuilder.setMessage(activity?.getString(R.string.pendientes_al_salir))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                    })

                val alert = dialogBuilder.create()
                alert.setTitle(activity?.getString(R.string.informacion))
                alert.show()
            }
        }
    }

    class AsyncTaskEliminarDatos(private var activity: MainActivity?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null

        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(context = activity?.applicationContext!!)
            var conteoDao = db?.conteoDao()
            conteoDao?.eliminar()
            if(sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
                if (sesionAplicacion?.tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                    //Bodega
                    var reconteoBodegaDao = db?.reconteoBodegaDao()
                    reconteoBodegaDao?.eliminarTodo()
                } else {
                    //Local
                    var reconteoLocalDao = db?.reconteoLocalDao()
                    reconteoLocalDao?.eliminar()
                }
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            val inventarioPreferences: SharedPreferences = activity!!.getSharedPreferences(Constantes.PREF_NAME, 0)
            inventarioPreferences.edit().clear().commit()
            activity!!.finish()
        }
    }

}