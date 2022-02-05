package com.example.syncfirebase

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import io.grpc.Context
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    //Se crea fuera de onCreate porque se usará en otros lugares
    private lateinit var imageView: ImageView
    private lateinit var iconView: ImageView
    private val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTakePhoto = findViewById<Button>(R.id.btnCapture)
        imageView = findViewById(R.id.ivFoto)
        iconView = findViewById(R.id.ivFoto2)

        btnTakePhoto.setOnClickListener {
            dispatchTakePhotoIntent()
        }
    }

    private fun dispatchTakePhotoIntent(){
        // Lo que hace el siguiente intent es buscar cualquier aplicación que mediante ese flag
        // levante la camara, todos los dispositivos hoy en día ya tienen por lo menos una
        // aplicación para tomar foto, sin embargo se debe manejar una exception con try y catch
        // por si no se tiene instalada ninguna.
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }catch (e: ActivityNotFoundException){
            Toast.makeText(this, "No tiene instalada ninguna app para tomar foto", Toast.LENGTH_SHORT).show()
        }
    }

    /*
    * Con este método se cacha el resultado de la actividad que se inicio con el método
    * "startActivityForResult", para lo cual se debe utilizar el requescode usado y evaluar el
    * resulcode de la actividad.
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){

            // El siguiente "as Bitmap es un casteo, ya que "data" vuelve del Intent" y puede ser
            // cualquier tipo de data que en este caso estamos casteando el resultado que viene de
            // la camara a un "Bitmap".
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Lo que hace posible el ajuste de la foto a los margenes creados del imageView es el
            // atributo scaleType cuyo valor es "centerCrop"
            imageView.setImageBitmap(imageBitmap)
            iconView.setImageBitmap(imageBitmap)

            uploadPicture(imageBitmap)
        }
    }

    /*
    * Este método recibe la foto que se tomó en formato de Bitmap y lo almacena en la storage
    */
    private fun uploadPicture(bitmap: Bitmap){
        //CREAR REFERENCIA (Es como una ubicacion) -> Se crea una instancia que a su ves nos permite
        // crear la referencia dentro de sotorage.
        val storageRef = FirebaseStorage.getInstance().reference
        //Tomando la referencia creada ya se puede ubicar el archivo, en este caso creando otra
        //carpeta llamada imagenesPrueba
        val imagesRef = storageRef.child("imagenesPrueba/${UUID.randomUUID()}.jpg")

        // Con imagesRef yo puedo poner archivos, subir información y para hacerlo tengo tres
        // formas: putBytes, putFile y putStream, siendo las 2 primeras las más usadas.
        // Con putFile se requiere obtener el path de la ubicación a la que queremos guardar el
        // archivo, esta ubicación es del directorio del celular pero vendría siendo algo como lo
        // siguiente en una pc windows, esto se conoce tambn como uri.
        // imagesRef.putFile(uri:"C://carpeta1/carpeta2//...
        // Con putBytes se sube una secuencia de bytes (En este caso ByteArray) a nuestro storage
        val baos = ByteArrayOutputStream()//baos = 'b'yte 'a'rray 'o'utput 's'tream
        //Lo que se debe hacer es lo siguiente: Tomamos la foto, la vamos a comprimir, se va a tomar
        // el baos, se va transormar esa foto en un conjunto de bytes y se va a subir a firestore.
        // Primero se comprime la foto, en este caso a un JPEG, se puede comprimir definiendo una
        // calidad de 0 a 100, siendo 100 la calidad mayor y baos el output donde se guardará la
        // imagen ya comprimida.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        // Se crea data para transformar nuestro baos en un byte array. Esto es importante ya que
        // el ByteArray es lo que necesitamos para subir nuestra foto a firestore.
        val data = baos.toByteArray()
        // El putBytes devuelve un "uploadTask", el cual puede ser usado para monitorear y manejar
        // la carga. Esto se logra ya que "uploadTask" puede devolver un evento de "success",
        // "progress" o "failure" en el momento de que se esté subiendo un archivo. También es
        // posible pausar o resumir el control de la carga en el momento en que se hace.
        val uploadTask = imagesRef.putBytes(data)//uploadTask es asincrono.

        //Este caso es el de carga exitosa
        // Se tona ek uploadTask y se le dice que se continue con el task para esperar el resultado
        uploadTask.continueWithTask { task ->
            if(!task.isSuccessful){
                task.exception?.let { exception ->
                    throw exception//Falta probar excepcion
                }
            }else{Log.d("Charlie", "EXITOSO")}
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUrl = task.result.toString()//Cotiene la uri de la imagen
                FirebaseFirestore.getInstance().collection("ciudades").document("PR").update(mapOf("imageUrl" to downloadUrl))
                Log.d("Charlie", "uploadPictureURL: $downloadUrl")
            }else{Log.d("Charlie", "No fue exitoso :(")}
        }
    }
}

