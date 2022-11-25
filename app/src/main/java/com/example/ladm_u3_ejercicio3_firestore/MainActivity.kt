package com.example.ladm_u3_ejercicio3_firestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.ladm_u3_ejercicio3_firestore.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var listaIDs = ArrayList<String>()
    var idActualizar = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.filtro.isVisible = false
        mostrar()
        binding.filtro.setOnClickListener {
            mostrar()
            binding.filtro.isVisible = false
        }

        binding.mostrar.setOnItemClickListener { adapterView, view, itemSeleccionado, l ->
            var idSeleccionado = listaIDs.get(itemSeleccionado)

            AlertDialog.Builder(this).setTitle("ATENCION")
                .setMessage("QUE DESEAS HACER?")
                .setPositiveButton("Eliminar"){d,i->
                    eliminar(idSeleccionado)
                }
                .setNeutralButton("Actualizar"){d,i->
                    actualizar(idSeleccionado)
                }
                .setNegativeButton("Nada"){d,i->

                }
                .show()
        }

        binding.buscar.setOnClickListener {
            if(binding.buscar.text.toString().startsWith("CANCE")){
                binding.nombre.setText("")
                binding.correo.setText("")
                binding.edad.setText("")
                binding.insertar.setText("INSERTAR")
                binding.buscar.setText("BUSCAR")
                idActualizar = ""
                return@setOnClickListener
            }
            //Codigo para construir un Linear Layout
            var layin = LinearLayout(this)
            var comboCampos = Spinner(this)
            var itemsCampos = ArrayList<String>()
            var claveBusqueda = EditText(this)

            itemsCampos.add("Nombre")
            itemsCampos.add("Correo")
            itemsCampos.add("Edad <")
            itemsCampos.add("Edad ==")
            itemsCampos.add("Edad >")

            comboCampos.adapter = ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,
                itemsCampos)

            layin.orientation = LinearLayout.VERTICAL
            claveBusqueda.setHint("CLAVE A BUSCAR")
            layin.addView(comboCampos)
            layin.addView(claveBusqueda)
            AlertDialog.Builder(this).setTitle("ATENCION")
                .setMessage("ELIJA CAMPO PARA BUSQUEDA")
                .setView(layin)
                .setPositiveButton("BUSCAR"){d,i->
                    consulta(comboCampos, claveBusqueda)
                }
                .setNeutralButton("CANCELAR"){d,i->}
                .show()
        }

        binding.insertar.setOnClickListener {
            if(binding.insertar.text.toString().startsWith("ACTUALIZAR")){
                actualizar2()
                return@setOnClickListener
            }

            var datos = hashMapOf(
                "nombre" to binding.nombre.text.toString(),
                "correo" to binding.correo.text.toString(),
                "edad" to binding.edad.text.toString().toInt(),
                "registrado" to Date()
            )

            FirebaseFirestore.getInstance().collection("personas")
                .add(datos)
                .addOnSuccessListener {
                    toas("SE INSERTO CON EXITO")
                    binding.nombre.setText("")
                    binding.correo.setText("")
                    binding.edad.setText("")
                }
                .addOnFailureListener {
                    aler(it.message!!)
                }
        }
    }

    private fun mostrar() {
        FirebaseFirestore.getInstance()
            .collection("personas")
            .addSnapshotListener { value, error ->
                if(error != null){
                    aler("NO SE PUDO REALIZAR LA CONSULTA")
                    return@addSnapshotListener
                }
                var lista = ArrayList<String>()
                listaIDs.clear()
                for(documento in value!!){
                    var cadena = documento.getString("nombre")+"\n"+
                            documento.get("edad").toString()+" -- "+
                            documento.getString("correo")
                    lista.add(cadena)
                    listaIDs.add(documento.id)
                }
                binding.mostrar.adapter = ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, lista)
            }
    }

    private fun consulta(comboCampos: Spinner, claveBusqueda: EditText) {
        var posicionCampoSeleccionado = comboCampos.selectedItemId.toInt()
        when(posicionCampoSeleccionado){
            0-> { //nombre
                FirebaseFirestore.getInstance().collection("personas")
                    .whereEqualTo("nombre", claveBusqueda.text.toString())
                    .get()
                    .addOnSuccessListener {
                        var resultado = ArrayList<String>()
                        for (documento in it!!){
                            var cad = documento.getString("nombre")
                            resultado.add(cad!!)
                        }
                        binding.mostrar.adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, resultado)
                    }
            }
            1-> {//correo
                FirebaseFirestore.getInstance().collection("personas")
                    .addSnapshotListener { value, error ->
                        var resultado = ArrayList<String>()
                        for (documento in value!!){
                            if (documento.getString("correo").toString().contains(claveBusqueda.text.toString())) {
                                var cad = documento.getString("correo")
                                resultado.add(cad!!)
                            }
                            binding.mostrar.adapter = ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_list_item_1, resultado
                            )
                        }
                    }
            }
            2-> { //Eddad menor
                FirebaseFirestore.getInstance().collection("personas")
                    .whereLessThan("edad", claveBusqueda.text.toString())
                    .get()
                    .addOnSuccessListener {
                        var resultado = ArrayList<String>()
                        for (documento in it!!){
                            var cad = documento.getString("edad")
                            resultado.add(cad!!)
                        }
                        binding.mostrar.adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, resultado)
                    }
            }
            3-> { //Edad igual
                FirebaseFirestore.getInstance().collection("personas")
                    .whereEqualTo("edad", claveBusqueda.text.toString())
                    .get()
                    .addOnSuccessListener {
                        var resultado = ArrayList<String>()
                        for (documento in it!!){
                            var cad = documento.getString("edad")
                            resultado.add(cad!!)
                        }
                        binding.mostrar.adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, resultado)
                    }
            }
            4-> { //Edad mayor
                FirebaseFirestore.getInstance().collection("personas")
                    .whereGreaterThan("edad", claveBusqueda.text.toString())
                    .get()
                    .addOnSuccessListener {
                        var resultado = ArrayList<String>()
                        for (documento in it!!){
                            var cad = documento.getString("edad")
                            resultado.add(cad!!)
                        }
                        binding.mostrar.adapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, resultado)
                    }
            }
        }
    }

    private fun actualizar2() {
        FirebaseFirestore.getInstance().collection("personas")
            .document(idActualizar)
            .update("nombre", binding.nombre.text.toString(),
            "correo", binding.correo.text.toString(),
            "edad", binding.edad.text.toString().toInt())
            .addOnSuccessListener {
                toas("SE ACTUALIZO CORRECTAMENTE")
                binding.nombre.setText("")
                binding.correo.setText("")
                binding.edad.setText("")
                binding.insertar.setText("INSERTAR")
                binding.buscar.setText("BUSCAR")
                idActualizar = ""
            }
            .addOnFailureListener {
                aler(it.message!!)
            }
    }

    private fun actualizar(idSeleccionado: String) {
        FirebaseFirestore.getInstance().collection("personas")
            .document(idSeleccionado)
            .get()
            .addOnSuccessListener {
                binding.nombre.setText(it.getString("nombre"))
                binding.correo.setText(it.getString("correo"))
                binding.edad.setText(it.get("edad").toString())
                binding.insertar.setText("ACTUALIZAR")
                binding.buscar.setText("CANCELAR ACTUALIZAR")
                idActualizar = idSeleccionado
            }
    }

    private fun eliminar(idSeleccionado: String) {
        FirebaseFirestore.getInstance().collection("personas")
            .document(idSeleccionado)
            .delete()
            .addOnSuccessListener {
                toas("SE BORRO!")
            }
            .addOnFailureListener {
                aler(it.message!!)
            }
    }

    fun toas(m:String){
        Toast.makeText(this,m,Toast.LENGTH_LONG).show()
    }

    fun aler(m:String){
        AlertDialog.Builder(this).setTitle("ATENCION").setMessage(m)
            .setPositiveButton("OK"){d,i->}
            .show()
    }
}