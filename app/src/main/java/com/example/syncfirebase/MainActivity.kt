package com.example.syncfirebase

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

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
        }
    }
}

