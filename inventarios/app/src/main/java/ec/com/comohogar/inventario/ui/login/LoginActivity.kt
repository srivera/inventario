package ec.com.comohogar.inventario.ui.login

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*

import ec.com.comohogar.inventario.adapter.LocalAdapter
import ec.com.comohogar.inventario.modelo.Empleado
import ec.com.comohogar.inventario.modelo.Lugar
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
import ec.com.comohogar.inventario.modelo.Usuario


class LoginActivity : AppCompatActivity(), View.OnKeyListener {

    private lateinit var loginViewModel: LoginViewModel

    private var spinnerLocal: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        spinnerLocal = findViewById<Spinner>(R.id.spinnerLocal)
        val usuario = findViewById<EditText>(R.id.editUsuario)
        val login = findViewById<Button>(R.id.login)

        usuario?.setOnKeyListener(this)
        spinnerLocal?.requestFocus()

        val call: Call<List<Lugar>> = ApiClient.getClient.consultarLocales()
        call.enqueue(object : Callback<List<Lugar>> {

            override fun onResponse(call: Call<List<Lugar>>?, response: Response<List<Lugar>>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                var spinnerAdapter: LocalAdapter = LocalAdapter(applicationContext!!, response.body())
                spinnerLocal?.adapter = spinnerAdapter
            }

            override fun onFailure(call: Call<List<Lugar>>, t: Throwable) {
                Log.i("error", "error")
            }

        })
    }


    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event?.action == KeyEvent.ACTION_UP){
            consultarUsuario()
        }
        return false
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
                    verificarReconteo()
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

        val call: Call<Usuario> = ApiClient.getClient.consultarAsignacionUsuario(editUsuario.text.toString().toLong(), deviceUniqueId.toString())
        call.enqueue(object : Callback<Usuario> {

            override fun onResponse(call: Call<Usuario>?, response: Response<Usuario>?) {
                Log.i("respuesta", response!!.body()!!.toString())
                val inventarioPreferences: SharedPreferences = getSharedPreferences(Constantes.PREF_NAME, 0)
                val prefsEditor = inventarioPreferences.edit()
                val gson = Gson()
                val json = gson.toJson(response.body())
                prefsEditor.putString(Constantes.USUARIO, json)
                prefsEditor.commit()
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Log.i("error", "error")
            }

        })
    }

}
