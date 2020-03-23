package ec.com.comohogar.inventario.ui.login

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.com.comohogar.inventario.MainActivity
import ec.com.comohogar.inventario.SesionAplicacion
import ec.com.comohogar.inventario.adapter.ConteoAdapter
import ec.com.comohogar.inventario.adapter.InventarioAdapter
import ec.com.comohogar.inventario.databinding.ActivityLoginBinding
import ec.com.comohogar.inventario.modelo.*

class LoginActivity : AppCompatActivity(), View.OnKeyListener {

    private var spinnerLocal: Spinner? = null

    private var spinnerInventario: Spinner? = null

    private var spinnerConteo: Spinner? = null

    private var sesionAplicacion: SesionAplicacion? = null

    private var listaInventario: List<Inventario>? = null

    private var listaConteo: List<Conteo>? = null

    private var tipo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val binding: ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        binding.setLifecycleOwner(this)
        binding.uiController = this

        sesionAplicacion = applicationContext as SesionAplicacion?

        var textTitulo = findViewById<TextView>(R.id.titulo)
        val editUsuario = findViewById<EditText>(R.id.editUsuario)
        spinnerLocal = findViewById<Spinner>(R.id.spinnerLocal)
        spinnerInventario = findViewById<Spinner>(R.id.spinnerInventario)
        spinnerConteo = findViewById<Spinner>(R.id.spinnerConteo)
        var btnSiguiente = findViewById<Button>(R.id.btnSiguiente)
        var btnAnterior = findViewById<Button>(R.id.btnAnterior)
        var textInventario = findViewById<TextView>(R.id.textInventario)
        var textConteo = findViewById<TextView>(R.id.textConteo)

        tipo = intent.getStringExtra(Constantes.ES_CONTEO_RECONTEO)

        if(tipo.equals(Constantes.ES_CONTEO)){
            textTitulo.setText("Conteo")
            spinnerInventario?.visibility = View.VISIBLE
            spinnerConteo?.visibility = View.VISIBLE
            textInventario?.visibility = View.VISIBLE
            textConteo?.visibility = View.VISIBLE

        }else{
            textTitulo.setText("Reconteo")
            spinnerInventario?.visibility = View.INVISIBLE
            spinnerConteo?.visibility = View.INVISIBLE
            textInventario?.visibility = View.INVISIBLE
            textConteo?.visibility = View.INVISIBLE
        }

        editUsuario?.setOnKeyListener(this)
        spinnerLocal?.requestFocus()

        btnSiguiente.setOnClickListener {
            if (!editUsuario?.text.isNullOrBlank()) {
                ingresar()
            }else{
                editUsuario.error = "Ingrese su usuario"
            }

        }

        btnAnterior.setOnClickListener {
            finish()
        }


        val call: Call<List<Lugar>> = ApiClient.getClient.consultarLocales()
        call.enqueue(object : Callback<List<Lugar>> {

            override fun onResponse(call: Call<List<Lugar>>?, response: Response<List<Lugar>>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                spinnerLocal?.adapter = LocalAdapter(applicationContext!!, response.body())
                sesionAplicacion?.listaLugares = response.body()
            }

            override fun onFailure(call: Call<List<Lugar>>, t: Throwable) {
                Log.i("error", "error")
            }

        })

        spinnerLocal?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                if(tipo.equals(Constantes.ES_CONTEO)) {
                    consultarInventarioPorLugar()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }


        spinnerInventario?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                consultarNumeroConteo()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }


    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP){
            consultarUsuario()
        }
        return false
    }

    fun ingresar() {
        val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
        val prefsEditor = inventarioPreferences.edit()
        val gson = Gson()
        prefsEditor.putString(Constantes.INVENTARIO, gson.toJson(listaInventario?.get(spinnerInventario?.selectedItemPosition!!)))
        prefsEditor.putString(Constantes.CONTEO, gson.toJson(listaConteo?.get(spinnerConteo?.selectedItemPosition!!)))
        prefsEditor.putString(Constantes.ES_CONTEO_RECONTEO, tipo)
        prefsEditor.commit()
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
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
                    prefsEditor.commit()
                    btnSiguiente.setEnabled(true)
                    if(tipo.equals(Constantes.ES_RECONTEO)) {
                        verificarReconteo()
                    }
                }else{
                    val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                    dialogBuilder.setMessage("El usuario no existe")
                        .setCancelable(false)
                        .setPositiveButton("OK", DialogInterface.OnClickListener {
                                dialog, id ->
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
                    verificarTipoInventario(asignacionUsuario?.binId?.toInt())

                }else{
                    if(tipo.equals(Constantes.ES_CONTEO)) {
                        consultarInventarioPorLugar()
                    }else{
                        val dialogBuilder = AlertDialog.Builder(this@LoginActivity)

                        dialogBuilder.setMessage("No tiene asignados reconteos, verifique.")
                            .setCancelable(false)
                            .setPositiveButton("OK", DialogInterface.OnClickListener {
                                    dialog, id ->
                            })

                        val alert = dialogBuilder.create()
                        alert.setTitle("Error")
                        alert.show()
                    }
                }
            }

            override fun onFailure(call: Call<List<AsignacionUsuario>>, t: Throwable) {
                Log.i("error", "error")
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
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
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
            }

            override fun onFailure(call: Call<List<Conteo>> , t: Throwable) {
                Log.i("error", "error")
            }

        })
    }
}
