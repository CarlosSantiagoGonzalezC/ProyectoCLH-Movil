package com.example.appmovilclh1

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {
    lateinit var txtCorreo: EditText
    lateinit var txtPassword: EditText
    lateinit var btnInicar: Button
    private val urlBase:String = "https://clhbackend1.000webhostapp.com/api/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtCorreo = findViewById(R.id.txtCorreo)
        txtPassword = findViewById(R.id.txtPassword)
        btnInicar = findViewById(R.id.btnIniciar)

        btnInicar.setOnClickListener{iniciar()}
    }

    /**
     * Función que realiza petición a la API para iniciar sesion
     */
    private fun iniciar() {
        /*val intent = Intent(this, VistaPrincipal::class.java)
        startActivity(intent)*/
        val url = urlBase + "auth"
        val queue = Volley.newRequestQueue(this)
        val progressBar = ProgressDialog.show(this, "Enviando datos...", "Espere por favor")
            val resultadoPost = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                progressBar.dismiss()
                Toast.makeText(this, "Sesión iniciada", Toast.LENGTH_LONG).show()
                println("RESPUESTA: ${response}")
                val intent = Intent(this, VistaPrincipal::class.java)
                startActivity(intent)
            }, Response.ErrorListener { error ->
                progressBar.dismiss()
                Toast.makeText(this, "Error al iniciar sesión: ${error.message} ", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? {
                val parametros = HashMap<String, String>()
                parametros.put("useCorreo", txtCorreo.text.toString())
                parametros.put("usePassword", txtPassword.text.toString())
                return parametros
            }
        }
        queue.add(resultadoPost)
    }
}