package ec.com.comohogar.inventario

import android.content.SharedPreferences
import android.os.AsyncTask
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
import retrofit2.http.Path
import java.util.*


class MainActivity : ScanActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private var sesionAplicacion: SesionAplicacion? = null

    var progressBar: ProgressBar? = null

    var tipo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        sesionAplicacion = applicationContext as SesionAplicacion?

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

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
        cargarSesion(inventarioPreferences)

        inicializarMenuFragment(tipoInventario, navView, drawerLayout, navController)


        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun inicializarMenuFragment(
        tipoInventario: Int,
        navView: NavigationView,
        drawerLayout: DrawerLayout,
        navController: NavController
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

    val r: Runnable = Runnable {


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
        // Inflate the menu; this adds items to the action bar if it is present.
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