package ec.com.comohogar.inventario

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import ec.com.comohogar.inventario.ui.inicial.InicialActivity
import ec.com.comohogar.inventario.util.Constantes

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 100

    private var sesionAplicacion: SesionAplicacion? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sesionAplicacion = applicationContext as SesionAplicacion?

        Handler().postDelayed({
            val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
            var tipoInventario = inventarioPreferences.getInt(Constantes.TIPO_INVENTARIO, -1)
            var tipo = inventarioPreferences.getString(Constantes.ES_CONTEO_RECONTEO, "")
            sesionAplicacion?.tipo = tipo
            sesionAplicacion?.tipoInventario = tipoInventario

            if(tipo.equals(Constantes.ES_RECONTEO)) {
                if (tipoInventario.equals(-1)) {
                    startActivity(Intent(this, InicialActivity::class.java))
                } else {
                    MainActivity.open(this, false)
                }
            }else if(tipo.equals(Constantes.ES_CONTEO)) {
                MainActivity.open(this, true)
            }else{
                startActivity(Intent(this, InicialActivity::class.java))
            }

            finish()
        }, SPLASH_TIME_OUT)
    }
}
