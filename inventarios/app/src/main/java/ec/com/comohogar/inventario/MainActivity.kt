package ec.com.comohogar.inventario

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import ec.com.comohogar.inventario.ui.config.ConfigActivity


class MainActivity : ScanActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var sesionAplicacion: SesionAplicacion? = null

    var tipo: String? = null

    private lateinit var navGraph: NavGraph

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
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        val graphInflater = navHostFragment?.navController?.navInflater
        navGraph = graphInflater?.inflate(R.navigation.mobile_navigation)!!

        var header = navView.getHeaderView(0)
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
           // sesionAplicacion?.usuId = asignacionUsuario.usuId
            sesionAplicacion?.numConteo = asignacionUsuario.numeroConteo.toInt()
        } else if (sesionAplicacion?.tipo.equals(Constantes.ES_CONTEO)) {
            val gson = Gson()
            val json = inventarioPreferences.getString(Constantes.CONTEO, "");
            val conteo = gson.fromJson(json, Conteo::class.java)
            sesionAplicacion?.binId = conteo.binId
            sesionAplicacion?.cinId = conteo.cinId
            //sesionAplicacion?.usuId = conteo.usuId
            sesionAplicacion?.numConteo = conteo.cinNumConteo.toInt()
        }

        textLocal?.text = sesionAplicacion?.nombreLocal

        val destination = if (intent.getBooleanExtra(
                ES_CONTEO,
                false
            )
        ) R.id.nav_conteo else if (tipoInventario!!.equals(Constantes.INVENTARIO_BODEGA)) R.id.nav_reconteo_bodega else R.id.nav_reconteo_local

        navGraph.startDestination = destination
        navController.graph = navGraph

        inicializarMenuFragment(sesionAplicacion?.tipoInventario, navView, drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun inicializarMenuFragment(
        tipoInventario: Int?,
        navView: NavigationView,
        drawerLayout: DrawerLayout
    ) {
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
                    30000
                )
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            procesarConteos()
                        }
                    },
                    0,
                    30000
                )
                navView.menu.clear()
                navView.inflateMenu(R.menu.menu_reconteo_bodega)
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_reconteo_bodega, R.id.nav_conteo,
                        R.id.nav_pendiente, R.id.nav_enviado, R.id.nav_error, R.id.nav_salir
                    ), drawerLayout
                )

            } else if (tipoInventario.equals(Constantes.INVENTARIO_LOCAL)) {
                val t = Timer()
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            procesarConteos()
                        }
                    },
                    0,
                    30000
                )
                navView.menu.clear()
                navView.inflateMenu(R.menu.menu_reconteo_local)
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_reconteo_local,
                        R.id.nav_pendiente, R.id.nav_enviado, R.id.nav_error, R.id.nav_salir
                    ), drawerLayout
                )
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
                30000
            )
            navView.menu.clear()
            navView.inflateMenu(R.menu.menu_conteo)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_conteo,
                    R.id.nav_pendiente, R.id.nav_enviado, R.id.nav_error, R.id.nav_salir
                ), drawerLayout
            )
        }
    }

    private fun procesarConteos() {
        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(this@MainActivity)
        var conteoDao = db?.conteoDao()
        val conteos = conteoDao?.getConteoPendiente()
        for (conteo in conteos!!) {
            val call: Call<Long> = ApiClient.getClient.ingresarConteo(
                conteo.usuId,
                conteo.cinId,  conteo.barra,
                conteo.cantidad, conteo.zona
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
    }

    private fun procesarReconteosBodega() {
        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(this@MainActivity)
        var reconteoBodegaDao = db?.reconteoBodegaDao()
        val reconteos = reconteoBodegaDao?.getReconteoBodegaPendiente()
        for (reconteo in reconteos!!) {
            val call: Call<Long> = ApiClient.getClient.insertarConteoUsuarioRuta(
                reconteo.usuIdAsignado.toLong(), reconteo.cinId,
                reconteo.rcoUbicacion, reconteo.barra,
                reconteo.cantidad.toLong(), reconteo.binId,
                reconteo.rcoId.toInt()
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
    }

    private fun procesarReconteosLocal() {
        var db: InventarioDatabase? = InventarioDatabase.getInventarioDataBase(this@MainActivity)
        var reconteoLocalDao = db?.reconteoLocalDao()
        val reconteos = reconteoLocalDao?.getReconteoLocalPendiente()
        for (reconteo in reconteos!!) {
            val call: Call<Long> = ApiClient.getClient.ingresarConteo(
               sesionAplicacion?.usuId, reconteo.cinId,
                reconteo.barra,
                reconteo.cantidad,
                ""
            )
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
        when (item.getItemId()) {
            R.id.action_settings -> {
                val intent = Intent(this, ConfigActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
