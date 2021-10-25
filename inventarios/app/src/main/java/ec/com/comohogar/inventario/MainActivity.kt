package ec.com.comohogar.inventario

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.TextView
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import ec.com.comohogar.inventario.modelo.*
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.scanner.ScanActivity
import ec.com.comohogar.inventario.ui.conteo.ConteoFragment
import ec.com.comohogar.inventario.ui.enviado.ConsultaEnviadoFragment
import ec.com.comohogar.inventario.ui.pendiente.ConsultaPendienteFragment
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.ui.reconteobodega.ReconteoBodegaFragment
import ec.com.comohogar.inventario.ui.reconteolocal.ReconteoLocalFragment
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import java.util.*
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavInflater
import ec.com.comohogar.inventario.ui.config.ConfigActivity
import ec.com.comohogar.inventario.ui.consultaconteo.ConsultaConteoUsuarioFragment
import ec.com.comohogar.inventario.ui.correccion.CorreccionFragment
import ec.com.comohogar.inventario.ui.error.ConsultaErrorFragment

class MainActivity : ScanActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    var sesionAplicacion: SesionAplicacion? = null

    var tipo: String? = null

    var navView: NavigationView? = null

    private lateinit var navGraph: NavGraph

    var navController: NavController? = null

    var navHostFragment: NavHostFragment? = null

    var graphInflater: NavInflater? = null

    var conteoPocketError: ConteoPocketError? = null

    var errorPendiente: Boolean? = false

    var idUsuarioConsulta: Long? = null

    var listaConteoHistorico: List<ConteoPocketError>? = null

    companion object {
        private const val ES_CONTEO = "esConteo"
        fun open(context: Context, esConteo: Boolean) {
            var intent = Intent(context, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent.apply {
                putExtra(ES_CONTEO, esConteo)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        sesionAplicacion = applicationContext as SesionAplicacion?

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        graphInflater = navHostFragment?.navController?.navInflater
        navGraph = graphInflater?.inflate(R.navigation.mobile_navigation)!!

        var header = navView?.getHeaderView(0)
        var textLocal = header?.findViewById<TextView>(R.id.textLocal);
        var textVersion = header?.findViewById<TextView>(R.id.textVersion);
        textVersion?.text = "Versión: " + BuildConfig.VERSION_NAME

        val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
        var tipoInventario = sesionAplicacion?.tipoInventario
        cargarSesion(inventarioPreferences)

        tipo = sesionAplicacion?.tipo

        if (sesionAplicacion?.tipo.equals(Constantes.ES_RECONTEO)) {
            val gson = Gson()
            val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
            val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)
            sesionAplicacion?.binId = asignacionUsuario.binId
            sesionAplicacion?.cinId = asignacionUsuario.cinId
         //   sesionAplicacion?.usuId = asignacionUsuario.usuId
            sesionAplicacion?.numConteo = asignacionUsuario.numeroConteo.toInt()
        } else if (sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
            val gson = Gson()
            val json = inventarioPreferences.getString(Constantes.CONTEO, "");
            val conteo = gson.fromJson(json, Conteo::class.java)
            sesionAplicacion?.binId = conteo.binId
            sesionAplicacion?.cinId = conteo.cinId
         //   sesionAplicacion?.usuId = conteo.usuId
            sesionAplicacion?.numConteo = conteo.cinNumConteo.toInt()
        }

        textLocal?.text = sesionAplicacion?.nombreLocal

        val destination = if (intent.getBooleanExtra(
                ES_CONTEO,
                false
            )
        ) R.id.nav_conteo else if (tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) R.id.nav_reconteo_bodega else R.id.nav_reconteo_bodega

        navGraph.startDestination = destination
        navController!!.graph = navGraph

        inicializarMenuFragment(sesionAplicacion?.tipoInventario, navView!!, drawerLayout)

        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView?.setupWithNavController(navController!!)

        val prefsEditor = inventarioPreferences.edit()
        prefsEditor.putString(Constantes.ENVIANDO_CONTEOS, "N")
        prefsEditor.commit()


    }

    private fun inicializarMenuFragment(
        tipoInventario: Int?,
        navView: NavigationView,
        drawerLayout: DrawerLayout
    ) {
        var esAuditor = sesionAplicacion?.empleado?.esAuditor
        val tiempo: Long = 60000
        if (tipo.equals(Constantes.ES_RECONTEO)) {
            if (tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) {
                val t = Timer()
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            procesarReconteosBodega()
                        }
                    },
                    0,
                    tiempo
                )
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            procesarConteos()
                        }
                    },
                    0,
                    tiempo
                )
                navView.menu.clear()

                if(esAuditor?.equals("S")!!) {
                    navView.inflateMenu(R.menu.menu_reconteo_bodega_auditor)
                    appBarConfiguration = AppBarConfiguration(
                        setOf(
                            R.id.nav_reconteo_bodega,
                            R.id.nav_conteo,
                            R.id.nav_pendiente,
                            R.id.nav_enviado,
                            R.id.nav_error,
                            R.id.nav_consulta_usuario,
                            R.id.nav_salir
                        ), drawerLayout
                    )
                }else{
                    navView.inflateMenu(R.menu.menu_reconteo_bodega)
                    appBarConfiguration = AppBarConfiguration(
                        setOf(
                            R.id.nav_reconteo_bodega,
                            R.id.nav_conteo,
                            R.id.nav_pendiente,
                            R.id.nav_enviado,
                            R.id.nav_error,
                            R.id.nav_salir
                        ), drawerLayout
                    )
                }

            } else if (tipoInventario.equals(Constantes.INVENTARIO_LOCAL)) {
                val t = Timer()
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            procesarReconteosBodega()
                        }
                    },
                    0,
                    tiempo
                )
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            procesarConteos()
                        }
                    },
                    0,
                    tiempo
                )
                navView.menu.clear()
                if(esAuditor?.equals("S")!!) {
                    navView.inflateMenu(R.menu.menu_reconteo_local_auditor)
                    appBarConfiguration = AppBarConfiguration(
                        setOf(
                            R.id.nav_reconteo_bodega,
                            R.id.nav_conteo,
                            R.id.nav_pendiente,
                            R.id.nav_enviado,
                            R.id.nav_error,
                            R.id.nav_consulta_usuario,
                            R.id.nav_salir
                        ), drawerLayout
                    )
                }else{
                    navView.inflateMenu(R.menu.menu_reconteo_local)
                    appBarConfiguration = AppBarConfiguration(
                        setOf(
                            R.id.nav_reconteo_bodega,
                            R.id.nav_conteo,
                            R.id.nav_pendiente,
                            R.id.nav_enviado,
                            R.id.nav_error,
                            R.id.nav_consulta_usuario,
                            R.id.nav_salir
                        ), drawerLayout
                    )
                }
            }
        } else {
            val t = Timer()
            t.scheduleAtFixedRate(
                object : TimerTask() {
                    override fun run() {
                        procesarConteos()
                    }
                },
                0,
                tiempo
            )
            navView.menu.clear()
            if(esAuditor?.equals("S")!!) {
                navView.inflateMenu(R.menu.menu_conteo_auditor)
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_conteo,
                        R.id.nav_pendiente,
                        R.id.nav_enviado,
                        R.id.nav_error,
                        R.id.nav_consulta_usuario,
                        R.id.nav_salir
                    ), drawerLayout
                )
            }else{
                navView.inflateMenu(R.menu.menu_conteo)
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_conteo,
                        R.id.nav_pendiente,
                        R.id.nav_enviado,
                        R.id.nav_error,
                        R.id.nav_salir
                    ), drawerLayout
                )
            }
        }
    }

    @Synchronized private fun procesarConteos() {
        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(this@MainActivity)
        var conteoDao = db?.conteoDao()
        val conteos = conteoDao?.getConteoPendiente()
        val inventarioPreferences: SharedPreferences =
            getSharedPreferences(Constantes.PREF_NAME, 0)
        val procesando = inventarioPreferences.getString(Constantes.ENVIANDO_CONTEOS, "N");


        Log.d("procesando afuera ANTES", procesando)
        if (procesando.equals("N")) {
            val prefsEditor = inventarioPreferences.edit()
            prefsEditor.putString(Constantes.ENVIANDO_CONTEOS, "S")
            prefsEditor.commit()
            val procesando = inventarioPreferences.getString(Constantes.ENVIANDO_CONTEOS, "N");
            Log.d("procesando adentro  INGRESOOOOOO", procesando)
            //   Thread.sleep(60000)
            for (conteo in conteos!!) {
                val call: Call<Long> = ApiClient.getClient.ingresarConteo(
                    conteo.usuId,
                    conteo.cinId, conteo.barra,
                    conteo.cantidad, conteo.fecha, conteo.zona
                )
                try {
                    val response = call.execute()
                    val apiResponse = response.body()
                    if (apiResponse!!.equals(1L)) {
                        conteo.estado = Constantes.ESTADO_ENVIADO
                        conteoDao?.actualizarConteo(conteo)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

            }

            val conteosCorreccion = conteoDao?.getConteoPendienteCorreccion()

           for (conteo in conteosCorreccion!!) {
                val call: Call<Long> = ApiClient.getClient.actualizarConteo(
                    conteo.usuId!!.toLong(),
                    conteo.numConteo!!.toInt(),
                    conteo.barra,
                    conteo.cantidad.toLong(),
                    conteo.fecha,
                    conteo.binId!!.toLong(),
                    conteo.cinId!!.toLong(),
                    conteo.pocId!!.toLong(),
                    conteo.zona
                )
                try {
                    val response = call.execute()
                    val apiResponse = response.body()
                    if (apiResponse!!.equals(1L)) {
                        conteo.estado = Constantes.ESTADO_ENVIADO
                        conteoDao?.actualizarConteo(conteo)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

            }



            prefsEditor.putString(Constantes.ENVIANDO_CONTEOS, "N")
            prefsEditor.commit()

          AsyncTaskConsultarError(this as MainActivity?).execute()


        }

    }

    fun procesarReconteosBodega() {
        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(this@MainActivity)
        var reconteoBodegaDao = db?.reconteoBodegaDao()
        val reconteos = reconteoBodegaDao?.getReconteoBodegaPendiente()
        val inventarioPreferences: SharedPreferences =
            getSharedPreferences(Constantes.PREF_NAME, 0)
        val procesando = inventarioPreferences.getString(Constantes.ENVIANDO_CONTEOS, "N");


        Log.d("procesando afuera bodega", procesando)
        if (procesando.equals("N")){
            val prefsEditor = inventarioPreferences.edit()
            prefsEditor.putString(Constantes.ENVIANDO_CONTEOS, "S")
            prefsEditor.commit()
            val procesando = inventarioPreferences.getString(Constantes.ENVIANDO_CONTEOS, "N");
            Log.d("procesando adentro bodega", procesando)
            for (reconteo in reconteos!!) {
                val call: Call<Long> = ApiClient.getClient.insertarConteoUsuarioRuta(
                    reconteo.usuIdAsignado.toLong(), reconteo.cinId,
                    reconteo.rcoUbicacion, reconteo.barra,
                    reconteo.cantidad.toLong(), reconteo.binId,
                    reconteo.rcoId.toInt(), reconteo.fecha
                )
                try {
                    val response = call.execute()
                    val apiResponse = response.body()
                    if (apiResponse!!.equals(1L)) {
                        reconteo.estado = Constantes.ESTADO_ENVIADO
                        reconteoBodegaDao?.actualizarConteo(reconteo)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            prefsEditor.putString(Constantes.ENVIANDO_CONTEOS, "N")
            prefsEditor.commit()
        }
    }

    private fun procesarReconteosLocal() {
        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(this@MainActivity)
        var reconteoLocalDao = db?.reconteoLocalDao()
        val reconteos = reconteoLocalDao?.getReconteoLocalPendiente()
        for (reconteo in reconteos!!) {
            val call: Call<Long> = ApiClient.getClient.ingresarConteo(
               sesionAplicacion?.usuId, reconteo.cinId,
                reconteo.barra,
                reconteo.cantidad,  reconteo.fecha, "")
            try {
                val response = call.execute()
                val apiResponse = response.body()
                if (apiResponse!!.equals(1L)) {
                    reconteo.estado = Constantes.ESTADO_ENVIADO
                    reconteoLocalDao?.actualizarConteo(reconteo)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun cargarSesion(inventarioPreferences: SharedPreferences) {
        val gson = Gson()
        var json = inventarioPreferences.getString(Constantes.EMPLEADO, "");
        val empleado = gson.fromJson(json, Empleado::class.java)
        sesionAplicacion?.empleado = empleado
        sesionAplicacion?.usuId = empleado?.usuId

        json = inventarioPreferences.getString(Constantes.CONTEO, "");
        val conteo = gson.fromJson(json, Conteo::class.java)
        sesionAplicacion?.conteo = conteo

        json = inventarioPreferences.getString(Constantes.INVENTARIO, "");
        val inventario = gson.fromJson(json, Inventario::class.java)
        sesionAplicacion?.inventario = inventario

        json = inventarioPreferences.getString(Constantes.LOCAL, "");
        val local = gson.fromJson(json, Lugar::class.java)
        sesionAplicacion?.nombreLocal = local?.lugNombre
    }


    fun reemplazarFragment(destination: Int, actualizar: Boolean){
        navGraph.startDestination = destination
        navController?.graph = navGraph
        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView?.setupWithNavController(navController!!)
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment

        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if (fragment is ConsultaErrorFragment) {
                fragment.refrescarLista(actualizar)
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun refrescarPantalla(codigoLeido: String) {
        Log.i("hijo", "hijo")
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment

        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if (fragment is ConteoFragment) {
                fragment.refrescarPantalla(codigoLeido)
            } else if (fragment is ReconteoBodegaFragment) {
                fragment.refrescarPantalla(codigoLeido)
            } else if (fragment is ReconteoLocalFragment) {
                fragment.refrescarPantalla(codigoLeido)
            } else if (fragment is ConsultaEnviadoFragment) {
                fragment.refrescarPantalla(codigoLeido)
            } else if (fragment is ConsultaPendienteFragment) {
                fragment.refrescarPantalla(codigoLeido)
            } else if (fragment is CorreccionFragment) {
                fragment.refrescarPantalla(codigoLeido)
            }
        }
    }

    override fun onBackPressed() {
        Log.i("hijo", "hijo")
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment

        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if (fragment is ConsultaConteoUsuarioFragment) {
                navGraph.startDestination = R.id.nav_consulta_usuario
                navController?.graph = navGraph
                setupActionBarWithNavController(navController!!, appBarConfiguration)
                navView?.setupWithNavController(navController!!)
            }else if (fragment is CorreccionFragment) {
                navGraph.startDestination = R.id.nav_error
                navController?.graph = navGraph
                setupActionBarWithNavController(navController!!, appBarConfiguration)
                navView?.setupWithNavController(navController!!)
            }
        }
    }


    override fun refrescarEstado(estado: String) {
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if (fragment is ConteoFragment) {
                fragment.refrescarEstado(estado)
            }else if (fragment is ReconteoBodegaFragment) {
                fragment.refrescarEstado(estado)
            } else if (fragment is ReconteoLocalFragment) {
                fragment.refrescarEstado(estado)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.getItemId().equals(R.id.action_settings)) {
                val intent = Intent(this, ConfigActivity::class.java)
                startActivity(intent)
                return true
        }else {
            Log.i("hijo", "hijo")
            val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment

            if (navHostFragment != null) {
                val childFragments = navHostFragment.childFragmentManager.fragments
                val fragment = childFragments.get(0)
                if (fragment is ConsultaConteoUsuarioFragment) {
                    navGraph.startDestination = R.id.nav_consulta_usuario
                    navController?.graph = navGraph
                    setupActionBarWithNavController(navController!!, appBarConfiguration)
                    navView?.setupWithNavController(navController!!)
                    return true
                }else if (fragment is CorreccionFragment) {
                    navGraph.startDestination = R.id.nav_error
                    navController?.graph = navGraph
                    setupActionBarWithNavController(navController!!, appBarConfiguration)
                    navView?.setupWithNavController(navController!!)
                    return true
                } else {
                     return super.onOptionsItemSelected(item)
                }
            } else {
                return super.onOptionsItemSelected(item)
            }
        }

    }

    class AsyncTaskConsultarError(private var activity: MainActivity?) : AsyncTask<String, String, Int>() {

        var sesionAplicacion: SesionAplicacion? = null
        var listaConteoHistorico: List<ConteoPocketError>? = null


        override fun doInBackground(vararg p0: String?): Int? {
            sesionAplicacion = activity?.applicationContext as SesionAplicacion?
            val call: Call<List<ConteoPocketError>> = ApiClient.getClient.consultarErrores(
                sesionAplicacion?.empleado!!.usuId, sesionAplicacion?.binId!!, sesionAplicacion?.numConteo!!.toLong())
            try {
                val response = call.execute()
                listaConteoHistorico = response.body()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            val navHostFragment = activity!!.supportFragmentManager.fragments.first() as? NavHostFragment
            val childFragments = navHostFragment?.childFragmentManager?.fragments
            val fragment = childFragments?.get(0)


            if(listaConteoHistorico == null){
                activity?.errorPendiente = false
            }else  if(!listaConteoHistorico?.isEmpty()!!){

                val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 30000)
                toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 800)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    toneG.release()
                }, (800 + 50).toLong())

                activity?.errorPendiente = true
                if (navHostFragment != null) {
                    if (fragment is ConteoFragment) {
                        fragment.refrescarError(true)
                    } else if (fragment is ReconteoBodegaFragment) {
                        fragment.refrescarError(true)
                    } else if (fragment is ReconteoLocalFragment) {
                        fragment.refrescarError(true)
                    } else if (fragment is ConsultaEnviadoFragment) {
                        fragment.refrescarError(true)
                    } else if (fragment is ConsultaPendienteFragment) {
                        fragment.refrescarError(true)
                    } else if (fragment is CorreccionFragment) {
                        fragment.refrescarError(true)
                    }
                }
            }else  if(listaConteoHistorico?.isEmpty()!!){
                activity?.errorPendiente = false
            }

        }
    }
}
