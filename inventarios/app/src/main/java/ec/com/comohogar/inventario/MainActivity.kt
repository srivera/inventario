package ec.com.comohogar.inventario

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
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import ec.com.comohogar.inventario.scanner.ScanActivity
import ec.com.comohogar.inventario.ui.conteo.ConteoFragment
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.ui.reconteobodega.ReconteoBodegaFragment
import ec.com.comohogar.inventario.ui.reconteolocal.ReconteoLocalFragment


class MainActivity : ScanActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
        var tipoInventario =  inventarioPreferences.getInt(Constantes.TIPO_INVENTARIO, 0)

        val mFragmentManager = supportFragmentManager

        if(tipoInventario.equals(Constantes.INVENTARIO_BODEGA)){
            navView.menu.clear()
            navView.inflateMenu(R.menu.menu_reconteo_bodega)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_reconteo_bodega,
                    R.id.nav_tools, R.id.nav_share, R.id.nav_send
                ), drawerLayout
            )
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, ReconteoBodegaFragment()).commit()

        }else if(tipoInventario.equals(Constantes.INVENTARIO_LOCAL)){
            navView.menu.clear()
            navView.inflateMenu(R.menu.menu_reconteo_local)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_reconteo_local,
                    R.id.nav_tools, R.id.nav_share, R.id.nav_send
                ), drawerLayout
            )
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, ReconteoLocalFragment()).commit()
        }else{
            navView.menu.clear()
            navView.inflateMenu(R.menu.menu_conteo)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_conteo,
                    R.id.nav_tools, R.id.nav_share, R.id.nav_send
                ), drawerLayout
            )
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, ConteoFragment()).commit()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

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
        if(navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if(fragment is ConteoFragment){
                fragment.refrescarPantalla(codigoLeido)
            }
        }
    }

    override fun refrescarEstado(estado: String) {
        Log.i("hijo", "hijo")
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        if(navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            val fragment = childFragments.get(0)
            if(fragment is ConteoFragment){
                fragment.refrescarEstado(estado)
            }
        }
    }
}
