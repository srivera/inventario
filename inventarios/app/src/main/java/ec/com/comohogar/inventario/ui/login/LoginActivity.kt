package ec.com.comohogar.inventario.ui.login

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

import ec.com.comohogar.inventario.adapter.LocalAdapter
import ec.com.comohogar.inventario.webservice.ApiClient
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import ec.com.comohogar.inventario.R
import ec.com.comohogar.inventario.util.Constantes
import android.provider.Settings.Secure
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.com.comohogar.inventario.BuildConfig
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.adapter.ConteoAdapter
import ec.com.comohogar.inventario.adapter.InventarioAdapter
import ec.com.comohogar.inventario.databinding.ActivityLoginBinding
import ec.com.comohogar.inventario.modelo.*

class LoginActivity : AppCompatActivity() {


    private var spinnerLocal: Spinner? = null

    private var spinnerInventario: Spinner? = null

    private var spinnerConteo: Spinner? = null

    private var sesionAplicacion: SesionAplicacion? = null

    private var listaInventario: List<Inventario>? = null

    private var listaConteo: List<Conteo>? = null

    private var tipo: String? = null

    private var binId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val binding: ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        binding.setLifecycleOwner(this)
        binding.uiController = this

        sesionAplicacion = applicationContext as SesionAplicacion?

        var textTitulo = findViewById<TextView>(R.id.titulo)
        val editUsuario = findViewById<EditText>(R.id.editUsuario)
        var btnSiguiente = findViewById<Button>(R.id.btnSiguiente)
        var btnAnterior = findViewById<Button>(R.id.btnAnterior)
        var textInventario = findViewById<TextView>(R.id.textInventario)
        var textConteo = findViewById<TextView>(R.id.textConteo)
        var textVersion = findViewById<TextView>(R.id.versionName)

        textVersion.setText("Versión: " +  BuildConfig.VERSION_NAME)

        spinnerLocal = findViewById<Spinner>(R.id.spinnerLocal)
        spinnerInventario = findViewById<Spinner>(R.id.spinnerInventario)
        spinnerConteo = findViewById<Spinner>(R.id.spinnerConteo)
        tipo = intent.getStringExtra(Constantes.ES_CONTEO_RECONTEO)

        if(tipo.equals(Constantes.ES_CONTEO)){
            textTitulo.setText(getString(R.string.menu_conteo))
            spinnerInventario?.visibility = View.VISIBLE
            spinnerConteo?.visibility = View.VISIBLE
            textInventario?.visibility = View.VISIBLE
            textConteo?.visibility = View.VISIBLE

        }else{
            textTitulo.setText(getString(R.string.menu_reconteo))
            spinnerInventario?.visibility = View.INVISIBLE
            spinnerConteo?.visibility = View.INVISIBLE
            textInventario?.visibility = View.INVISIBLE
            textConteo?.visibility = View.INVISIBLE
        }

        btnSiguiente.setOnClickListener {
            if (!editUsuario?.text.isNullOrBlank()) {
                ingresar()
            }else{
                editUsuario.error = getString(R.string.ingrese_usuario)
            }
        }

        btnAnterior.setOnClickListener {
            finish()
        }

        spinnerLocal?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                if(tipo.equals(Constantes.ES_CONTEO)) {
                    consultarInventarioPorLugar()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        spinnerInventario?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                consultarNumeroConteo()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        consultarLocales()
    }

    private fun consultarLocales() {
        val call: Call<List<Lugar>> = ApiClient.getClient.consultarLocales()
        call.enqueue(object : Callback<List<Lugar>> {

            override fun onResponse(call: Call<List<Lugar>>?, response: Response<List<Lugar>>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                spinnerLocal?.adapter = LocalAdapter(applicationContext!!, response.body())
                sesionAplicacion?.listaLugares = response.body()
            }

            override fun onFailure(call: Call<List<Lugar>>, t: Throwable) {
                Log.i("error", "error")
                /*val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                dialogBuilder.setMessage(getString(R.string.error_wifi))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                        consultarLocales()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Error")
                alert.show()*/
            }

        })
    }


    fun ingresar() {
        val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
        val gson = Gson()
        val json = inventarioPreferences.getString(Constantes.EMPLEADO, "");
        val empleado = gson.fromJson(json, Empleado::class.java)
        val prefsEditor = inventarioPreferences.edit()
        prefsEditor.putString(Constantes.LOCAL, gson.toJson(sesionAplicacion?.listaLugares?.get(spinnerLocal?.selectedItemPosition!!)))
        prefsEditor.putString(Constantes.INVENTARIO, gson.toJson(listaInventario?.get(spinnerInventario?.selectedItemPosition!!)))
        prefsEditor.putString(Constantes.CONTEO, gson.toJson(listaConteo?.get(spinnerConteo?.selectedItemPosition!!)))
        prefsEditor.putString(Constantes.ES_CONTEO_RECONTEO, tipo)
        prefsEditor.commit()
        sesionAplicacion?.tipo = tipo

        if(empleado?.usuId == null || empleado?.empCodigo.isNullOrBlank()){
            consultarUsuario()
        }else{
            if(tipo.equals(Constantes.ES_CONTEO)){
                binId = listaConteo?.get(spinnerConteo?.selectedItemPosition!!)?.binId?.toInt()
                verificarTipoInventario(binId)
                MainActivity.open(this, true)
            }else{
                MainActivity.open(this, false)
            }
        }

    }

    private fun consultarUsuario() {
        val call: Call<Empleado> = ApiClient.getClient.consultarUsuario(editUsuario.text.toString().toLong())
        call.enqueue(object : Callback<Empleado> {

            override fun onResponse(call: Call<Empleado>?, response: Response<Empleado>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                val empleado = response!!.body()
                if(!empleado?.usuId?.equals(0L)!!) {
                    val inventarioPreferences: SharedPreferences =
                        getSharedPreferences(Constantes.PREF_NAME, 0)
                    val prefsEditor = inventarioPreferences.edit()
                    val gson = Gson()
                    val json = gson.toJson(response.body())
                    prefsEditor.putString(Constantes.EMPLEADO, json)
                    prefsEditor.putString(Constantes.ENVIANDO_CONTEOS, "N")
                    prefsEditor.commit()
                    btnSiguiente.setEnabled(true)
                    if(tipo.equals(Constantes.ES_RECONTEO)) {
                        verificarReconteo()
                    }else{
                        verificarTipoInventario(binId)
                    }
                }else{
                    val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                    dialogBuilder.setMessage(getString(R.string.usuario_no_existe))
                        .setCancelable(false)
                        .setPositiveButton("OK", DialogInterface.OnClickListener {
                                dialog, id ->
                            val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
                            inventarioPreferences.edit().clear().commit()
                        })

                    val alert = dialogBuilder.create()
                    alert.setTitle("Error")
                    alert.show()
                }
            }

            override fun onFailure(call: Call<Empleado>, t: Throwable) {
                Log.i("error", "error")
            }
        })
    }


    private fun verificarReconteo() {
        val deviceUniqueId = Secure.getString(getContentResolver(), Secure.ANDROID_ID)

        val call: Call<List<AsignacionUsuario>> = ApiClient.getClient.consultarAsignacionUsuario(editUsuario.text.toString().toLong(), deviceUniqueId.toString())
        call.enqueue(object : Callback<List<AsignacionUsuario>> {

            override fun onResponse(call: Call<List<AsignacionUsuario>>?, response: Response<List<AsignacionUsuario>>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                if(response!!.body()!!.size > 0) {
                    val asignacionUsuario = response.body()?.get(0)
                    val inventarioPreferences: SharedPreferences =
                        getSharedPreferences(Constantes.PREF_NAME, 0)
                    val prefsEditor = inventarioPreferences.edit()
                    val gson = Gson()
                    val json = gson.toJson(asignacionUsuario)
                    prefsEditor.putString(Constantes.ASIGNACION_USUARIO, json)
                    prefsEditor.commit()
                    consultarLocalInventario(asignacionUsuario?.binId?.toInt())
                    verificarTipoInventario(asignacionUsuario?.binId?.toInt())

                }else{
                    if(tipo.equals(Constantes.ES_CONTEO)) {
                        consultarInventarioPorLugar()
                    }else{
                        val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                        dialogBuilder.setMessage(getString(R.string.no_reconteos))
                            .setCancelable(false)
                            .setPositiveButton("OK", DialogInterface.OnClickListener {
                                    dialog, id ->
                                val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
                                inventarioPreferences.edit().clear().commit()
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Error")
                        alert.show()
                    }
                }
            }

            override fun onFailure(call: Call<List<AsignacionUsuario>>, t: Throwable) {
                Log.i("error", "error")
                val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                dialogBuilder.setMessage(getString(R.string.no_reconteos))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener {
                            dialog, id ->
                        val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
                        inventarioPreferences.edit().clear().commit()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Error")
                alert.show()
            }
        })
    }

    private fun verificarTipoInventario(idInventario: Int?) {

        val call: Call<Int> = ApiClient.getClient.binTipoInventario(idInventario)
        call.enqueue(object : Callback<Int> {

            override fun onResponse(call: Call<Int>?, response: Response<Int>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                var tipoInventario = response.body().toString().toInt()
                val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
                val prefsEditor = inventarioPreferences.edit()
                prefsEditor.putInt(Constantes.TIPO_INVENTARIO, tipoInventario)
                prefsEditor.commit()
                sesionAplicacion?.tipoInventario = tipoInventario

                if(tipo.equals(Constantes.ES_RECONTEO)){
                    MainActivity.open(this@LoginActivity, false)
                }else{
                    MainActivity.open(this@LoginActivity, true)
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.i("error", "error")
            }

        })
    }


    private fun consultarInventarioPorLugar() {

        val call: Call<List<Inventario>> = ApiClient.getClient.consultarInventarioLugar(sesionAplicacion?.listaLugares?.get(spinnerLocal?.selectedItemPosition!!)?.lugId)
        call.enqueue(object : Callback<List<Inventario>>  {

            override fun onResponse(call: Call<List<Inventario>> ?, response: Response<List<Inventario>> ?) {
                Log.i("respuesta", response!!.body()!!.toString())
                spinnerInventario?.adapter = InventarioAdapter(applicationContext!!, response.body())
                listaInventario = response.body()!!
                if(response.body()!!.size == 1){
                    consultarNumeroConteo()
                }
            }

            override fun onFailure(call: Call<List<Inventario>> , t: Throwable) {
                Log.i("error", "error")
            }
        })
    }

    private fun consultarNumeroConteo() {

        val call: Call<List<Conteo>> = ApiClient.getClient.consultarNumeroConteo(listaInventario?.get(spinnerInventario?.selectedItemPosition!!)?.binId)
        call.enqueue(object : Callback<List<Conteo>>  {

            override fun onResponse(call: Call<List<Conteo>> ?, response: Response<List<Conteo>> ?) {
                Log.i("respuesta", response!!.body()!!.toString())
                spinnerConteo?.adapter = ConteoAdapter(applicationContext!!, response.body())
                listaConteo = response.body()

                val inventarioPreferences: SharedPreferences =
                    getSharedPreferences(Constantes.PREF_NAME, 0)
                val prefsEditor = inventarioPreferences.edit()
                val gson = Gson()
                val json = gson.toJson(response.body()?.get(0))
                prefsEditor.putString(Constantes.CONTEO, json)
                prefsEditor.commit()
                binId = response.body()?.get(0)?.binId?.toInt()
            }

            override fun onFailure(call: Call<List<Conteo>> , t: Throwable) {
                Log.i("error", "error")
            }

        })
    }

    private fun consultarLocalInventario(idInventario: Int?) {
        val call: Call<Lugar> = ApiClient.getClient.consultarLugarPorInventario(idInventario?.toLong()!!)
        call.enqueue(object : Callback<Lugar> {

            override fun onResponse(call: Call<Lugar>?, response: Response<Lugar>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                val local = response.body()
                val gson = Gson()
                val json = gson.toJson(local)

                val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
                val prefsEditor = inventarioPreferences.edit()
                prefsEditor.putString(Constantes.LOCAL, json)
                prefsEditor.commit()
            }

            override fun onFailure(call: Call<Lugar>, t: Throwable) {
                Log.i("error", "error")
                val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                dialogBuilder.setMessage(getString(R.string.error_wifi))
                    .setCancelable(false)
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                        consultarLocales()
                    })

                val alert = dialogBuilder.create()
                alert.setTitle("Error")
                alert.show()
            }

        })
    }
}
