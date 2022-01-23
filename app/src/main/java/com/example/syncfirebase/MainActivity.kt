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

    private fun uploadPicture(bitmap: Bitmap){
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("imagenesPrueba/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imagesRef.putBytes(data)

        uploadTask.continueWithTask { task ->
            if(!task.isSuccessful){
                task.exception?.let { exception ->
                    throw exception
                }
            }else{Log.d("Charlie", "EXITOSO")}
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUrl = task.result.toString()
                FirebaseFirestore.getInstance().collection("ciudades").document("PR").update(mapOf("imageUrl" to downloadUrl))
                Log.d("Charlie", "uploadPictureURL: $downloadUrl")
            }else{Log.d("Charlie", "No fue exitoso :(")}
        }
    }
}

