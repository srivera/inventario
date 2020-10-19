package ec.com.comohogar.inventario.ui.config

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ec.com.comohogar.inventario.BuildConfig
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.ui.login.LoginActivity
import ec.com.comohogar.inventario.util.Constantes
import ec.com.comohogar.inventario.webservice.ApiClient
import java.util.ArrayList

class ConfigActivity  : AppCompatActivity() {

    private var sesionAplicacion: SesionAplicacion? = null

    private var posicion: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        var btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val listView = findViewById(android.R.id.list) as ListView
        var textVersion = findViewById<TextView>(R.id.versionName)

        textVersion.setText("Versión: " +  BuildConfig.VERSION_NAME)

        val opciones: MutableList<String> = ArrayList<String>()
        opciones?.add("Wifi")
        opciones?.add("Internet")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, opciones)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        btnGuardar.setOnClickListener {
            val inventarioPreferences: SharedPreferences =
                getSharedPreferences(Constantes.PREF_NAME, 0)
            val prefsEditor = inventarioPreferences.edit()

            if(posicion == 0) {
                prefsEditor.putString(Constantes.URL_CONEXION, ApiClient.BASE_URL_WIFI)
                ApiClient.BASE_URL = ApiClient.BASE_URL_WIFI
            }else{
                prefsEditor.putString(Constantes.URL_CONEXION, ApiClient.BASE_URL_INTERNET)
                ApiClient.BASE_URL = ApiClient.BASE_URL_INTERNET
            }
            prefsEditor.commit()
            finish()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            posicion = position
        }
    }
}