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
import android.widget.ProgressBar
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import ec.com.comohogar.inventario.modelo.AsignacionUsuario
import ec.com.comohogar.inventario.modelo.Conteo
import ec.com.comohogar.inventario.modelo.Empleado
import ec.com.comohogar.inventario.modelo.Inventario
import ec.com.comohogar.inventario.persistencia.InventarioDatabase
import ec.com.comohogar.inventario.scanner.ScanActivity
import ec.com.comohogar.inventario.ui.conteo.ConteoFragment
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.ui.reconteobodega.ReconteoBodegaFragment
import ec.com.comohogar.inventario.ui.reconteolocal.ReconteoLocalFragment
import ec.com.comohogar.inventario.webservice.ApiClient
import retrofit2.Call
import java.util.*


class MainActivity : ScanActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var sesionAplicacion: SesionAplicacion? = null

    var progressBar: ProgressBar? = null

    var tipo: String? = null

    private lateinit var navGraph: NavGraph

    private lateinit var navController: NavController

    companion object {
        private const val ES_CONTEO = "esConteo"
        fun open(context: Context, esConteo: Boolean) {
            context.startActivity(Intent(context, MainActivity::class.java).apply {
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

        val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
        var tipoInventario = inventarioPreferences.getInt(Constantes.TIPO_INVENTARIO, 0)
        tipo = inventarioPreferences.getString(Constantes.ES_CONTEO_RECONTEO, "")
        if (tipo.equals(Constantes.ES_RECONTEO)) {
            val gson = Gson()
            val json = inventarioPreferences.getString(Constantes.ASIGNACION_USUARIO, "");
            val asignacionUsuario = gson.fromJson(json, AsignacionUsuario::class.java)
            sesionAplicacion?.binId = asignacionUsuario.binId
            sesionAplicacion?.cinId = asignacionUsuario.cinId
            sesionAplicacion?.usuId = asignacionUsuario.usuId
            sesionAplicacion?.numConteo = asignacionUsuario.numeroConteo.toInt()
        } else if (tipo.equals(Constantes.ES_CONTEO)) {
            val gson = Gson()
            val json = inventarioPreferences.getString(Constantes.CONTEO, "");
            val conteo = gson.fromJson(json, Conteo::class.java)
            sesionAplicacion?.binId = conteo.binId
            sesionAplicacion?.cinId = conteo.cinId
            sesionAplicacion?.usuId = conteo.usuId
            sesionAplicacion?.numConteo = conteo.cinNumConteo.toInt()
        } else {

        }

        val gson = Gson()
        val json = inventarioPreferences.getString(Constantes.EMPLEADO, "");
        val empleado = gson.fromJson(json, Empleado::class.java)
        sesionAplicacion?.empleado = empleado

        cargarSesion(inventarioPreferences)

       // inicializarMenuFragment(tipoInventario, navView, drawerLayout)

        val destination = if (intent.getBooleanExtra(
                ES_CONTEO,
                false
            )
        ) R.id.nav_conteo else R.id.nav_reconteo_bodega
        navGraph.startDestination = destination
        navController.graph = navGraph


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_conteo,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun inicializarMenuFragment(
        tipoInventario: Int,
        navView: NavigationView,
        drawerLayout: DrawerLayout
    ) {
        val mFragmentManager = supportFragmentManager

        if (tipo.equals(Constantes.ES_RECONTEO)) {
            if (tipoInventario.equals(Constantes.INVENTARIO_BODEGA)) {
                val t = Timer()
                t.scheduleAtFixedRate(
                    object : TimerTask() {

                        override fun run() {
                            procesarReconteosBodega()
                        }

                    },
                    0,
                    10000
                )
                navView.menu.clear()
                navView.inflateMenu(R.menu.menu_reconteo_bodega)
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_reconteo_bodega,
                        R.id.nav_tools, R.id.nav_share, R.id.nav_send
                    ), drawerLayout
                )
                val fragmentTransaction = mFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.nav_host_fragment, ReconteoBodegaFragment())
                    .commit()
                //supportFragmentManager.fragments.removeAt(0)
                //supportFragmentManager.fragments.add(0, ReconteoBodegaFragment())

                val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
                val graphInflater = navHostFragment?.navController?.navInflater
                navGraph = graphInflater?.inflate(R.navigation.mobile_navigation)!!
               // navController = navHostFragment.navController

                navGraph.startDestination = R.id.nav_reconteo_bodega
                navController.graph = navGraph


            } else if (tipoInventario.equals(Constantes.INVENTARIO_LOCAL)) {
                navView.menu.clear()
                navView.inflateMenu(R.menu.menu_reconteo_local)
                appBarConfiguration = AppBarConfiguration(
                    setOf(
                        R.id.nav_reconteo_local,
                        R.id.nav_tools, R.id.nav_share, R.id.nav_send
                    ), drawerLayout
                )
                val fragmentTransaction = mFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.nav_host_fragment, ReconteoLocalFragment())
                    .commit()
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
                10000
            )

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_conteo,
                    R.id.nav_tools, R.id.nav_share, R.id.nav_send
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
                conteo.numConteo?.toLong(), conteo.zona, conteo.barra,
                conteo.cantidad
            )

            try {
                val response = call.execute()
                val apiResponse = response.body()
                if (apiResponse!!.equals(1L)) {
                    conteo.estado = Constantes.ESTADO_ENVIADO
                    conteoDao?.actualizarConteo(conteo)
                }

                System.out.println(apiResponse)
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
                reconteo.cantidad.toLong(),reconteo.binId,
                reconteo.rcoId.toInt()
            )
            try {
                val response = call.execute()
                val apiResponse = response.body()
                if (apiResponse!!.equals(1L)) {
                    reconteo.estado = Constantes.ESTADO_ENVIADO
                    reconteoBodegaDao?.actualizarConteo(reconteo)
                }

                System.out.println(apiResponse)
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

        json = inventarioPreferences.getString(Constantes.CONTEO, "");
        val conteo = gson.fromJson(json, Conteo::class.java)
        sesionAplicacion?.conteo = conteo

        json = inventarioPreferences.getString(Constantes.INVENTARIO, "");
        val inventario = gson.fromJson(json, Inventario::class.java)
        sesionAplicacion?.inventario = inventario
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
            }
        }
    }

    override fun refrescarEstado(estado: String) {
        Log.i("hijo", "hijo")
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if (fragment is ConteoFragment) {
                fragment.refrescarEstado(estado)
            }
        }
    }


}