package com.example.appmovilclh1.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.appmovilclh1.R
import com.example.appmovilclh1.VistaPrincipal
import com.example.appmovilclh1.databinding.FragmentHomeBinding
import com.example.appmovilclh1.modelo.Categoria
import org.json.JSONArray
import org.json.JSONException
import java.io.ByteArrayOutputStream

class HomeFragment : Fragment() {
    lateinit var txtNombre: EditText
    lateinit var txtCodigo: EditText
    lateinit var txtCantidad: EditText
    lateinit var txtPrecio: EditText
    lateinit var txtDescripcion: EditText
    lateinit var cbCategoria: Spinner
    lateinit var btnSubir: Button
    lateinit var btnAgregar: Button
    lateinit var ivFoto:ImageView
    private var idCategoria:Int=0
    private lateinit var uriFoto:Uri
    private var fotoBase64: String=""
    private var bandera: Boolean = false
    private lateinit var listaCategorias:MutableList<Categoria>
    private var PICK_IMAGE = 1
    private var STORAGE_PERMISSION_CODE = 101
    private lateinit var bitmap: Bitmap
    private val urlBase:String = "https://clhbackend1.000webhostapp.com/api/"

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        txtNombre = binding.txtNombreProdu
        txtCodigo = binding.txtCodigoProdu
        txtCantidad = binding.txtCantidadProdu
        txtPrecio = binding.txtPrecioProdu
        txtDescripcion = binding.txtDescripcionProdu
        cbCategoria = binding.cbCategoriaProdu
        btnSubir = binding.btnSubir
        btnAgregar = binding.btnAgregar
        ivFoto = binding.ivFoto
        listaCategorias = mutableListOf<Categoria>()

        /*Se llama a la siguiente función para consumir la api que nos
        retorna las categorias y las guarda en la listaCategorias*/
        obtenerCategorias()

        btnSubir.setOnClickListener{abrirGaleria()}
        btnAgregar.setOnClickListener{agregar()}

        cbCategoria.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                idCategoria = listaCategorias[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Obtener las categorias al cargar el HomeFragment
     */
    private fun obtenerCategorias() {
        val url = urlBase + "category/read"
        val queue = Volley.newRequestQueue(requireContext())
        val jsonCategorias = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener<JSONArray>{ response->
                try {
                    val jsonArray = response
                    for(i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("id")
                        val nombre = jsonObject.getString("catNombre")
                        var categoria = Categoria(id.toInt(), nombre)
                        listaCategorias.add(categoria)
                    }
                    val adaptador = ArrayAdapter<Categoria>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaCategorias)
                    adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    cbCategoria.adapter = adaptador
                } catch (e: JSONException){
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_LONG).show()
            })
        queue.add(jsonCategorias)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    /**
     * Función que realiza petición a la API para agregar producto
     */
    private fun agregar() {
        /*val intent = Intent(this, VistaPrincipal::class.java)
        startActivity(intent)*/
        val url = urlBase + "product/create"
        val queue = Volley.newRequestQueue(requireContext())
        val progressBar = ProgressDialog.show(requireContext(), "Enviando datos...", "Espere por favor")
        val resultadoPost = object : StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                progressBar.dismiss()
                Toast.makeText(requireContext(), "Producto creado correctamente", Toast.LENGTH_LONG).show()
                limpiar()
            }, Response.ErrorListener { error ->
                progressBar.dismiss()
                Toast.makeText(requireContext(), "Error al agregar producto: ${error.message} ", Toast.LENGTH_LONG).show()
            }){
            override fun getParams(): MutableMap<String, String>? {
                val parametros = HashMap<String, String>()
                val foto = bitmapToString(bitmap)
                parametros.put("proNombre", txtNombre.text.toString())
                parametros.put("proCodigo", txtCodigo.text.toString())
                parametros.put("proCantDisponible", txtCantidad.text.toString())
                parametros.put("proPrecio", txtPrecio.text.toString())
                parametros.put("proDescripcion", txtDescripcion.text.toString())
                parametros.put("proImagen", foto)
                parametros.put("category_id", idCategoria.toString())
                parametros.put("user_id", "1")
                return parametros
            }
        }
        queue.add(resultadoPost)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null){
            val imageUri: Uri? = data.data
            ivFoto.setImageURI(imageUri)
            ivFoto.invalidate()
            val drawable = ivFoto.drawable
            bitmap = drawable.toBitmap()
        }
    }

    /**
     * Función que limpia todos los campos
     */
    private fun limpiar() {
        txtCodigo.text.clear()
        txtNombre.text.clear()
        txtPrecio.text.clear()
        txtCantidad.text.clear()
        txtDescripcion.text.clear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                abrirGaleria()
            } else {
                Toast.makeText(requireContext(), "No tiene permisos", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Funcion que convierte una imagen tipo bitmap a String
     */
    fun bitmapToString(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Funcion que convierte una String a Bitmap
     */
    fun stringToBitmap(encodedString: String): Bitmap {
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}