package ec.com.comohogar.inventario.ui.inicial

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.ui.login.LoginActivity
import ec.com.comohogar.inventario.util.Constantes
import java.util.ArrayList
import ec.com.comohogar.inventario.BuildConfig


class InicialActivity  : AppCompatActivity() {

    private var sesionAplicacion: SesionAplicacion? = null

    private var posicion: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicial)

        var btnSiguiente = findViewById<Button>(R.id.btnSiguiente)
        var textTitulo = findViewById<TextView>(R.id.titulo)
        val listView = findViewById(android.R.id.list) as ListView
        var textVersion = findViewById<TextView>(R.id.versionName)

        textTitulo.setText(Constantes.TITULO_INICIAL)
        textVersion.setText("Versión: " +  BuildConfig.VERSION_NAME)

        val opciones: MutableList<String> = ArrayList<String>()
        opciones?.add("Conteo")
        opciones?.add("Reconteo")

        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, opciones)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        btnSiguiente.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            if(posicion == 0) {
                intent.putExtra(Constantes.ES_CONTEO_RECONTEO, Constantes.ES_CONTEO)
            }else{
                intent.putExtra(Constantes.ES_CONTEO_RECONTEO, Constantes.ES_RECONTEO)
            }
            startActivity(intent)
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener {parent,view, position, id ->
            // Get the selected item text from ListView
            posicion = position
            btnSiguiente.setEnabled(true)
        }
    }
}