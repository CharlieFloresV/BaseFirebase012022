package com.example.syncfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Obtener instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        //Leer datos de un documento ¡¡¡UNA ÚNICA VES!!!
        db.collection("ciudades").document("PR").get().addOnSuccessListener { document ->
            document?.let {
                /*
                * Esta es una forma de recuperar un documento e ir accediendo a cada valor con su get...
                * val population = document.getLong("population")
                *
                * val color = document.getString("color")
                * Log.d("Charlie", "Valor recuperado ${document.data}")
                * Log.d("Charlie", "Valor de  population = $population")
                * Log.d("Charlie", "Valor de color =  $color")
                */

                /*
                * Esta es otr forma de acceder, primero al objeto y después a sus datos por medio
                * del objeto
                */
                val ciudad = document.toObject(Ciudad::class.java)
                Log.d("Charlie", "Valor recuperado $ciudad")
                Log.d("Charlie", "population: ${ciudad?.population}")
                Log.d("Charlie", "color: ${ciudad?.color}")
                Log.d("Charlie", "clima: ${ciudad?.clima}")
            }
        }.addOnFailureListener { exception ->
            Log.d("FBerror", exception.toString())
        }

        //Leer datos de un documento ¡¡¡EN TIEMPO REAL!!!
        db.collection("ciudades").document("PR").addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            documentSnapshot?.let {document ->
                val ciudad = document.toObject(Ciudad::class.java)//Ciudad es la clase creada más abajo, se hace de esta forma por comodidad
                Log.d("Charlie", "Sync Valor recuperado $ciudad")
                Log.d("Charlie", "Sync population: ${ciudad?.population}")
                Log.d("Charlie", "Sync color: ${ciudad?.color}")
                Log.d("Charlie", "Sync clima: ${ciudad?.clima}")
            }
        }

        //Agregar un documento con datos
        //Importante: Si el documento ya existe lo sobreescribirá y si lo que le pasemos al set() no
        // tiene alguno de los datos que maneja lo inicializará como lo indique la data class si es
        // que fue definido un valor default.
        db.collection("ciudades").document("EM").set(Ciudad(5000, "rojo")).addOnSuccessListener {
            Log.d("Charlie", "Ciudad agregada correctamente")
        }.addOnFailureListener { exception ->
            Log.d("FBerror", exception.toString())
        }

    }
}

/*
* Una forma cómo para trabajar es creando una clase para nuestro documento que queremos manipular,
* de esta forma tendremos presente de manera clara los datos que contiene e incluso controlar
* sus valores por default si se agrega un nuevo dato.
*  */
data class Ciudad(val population: Int = 0,
                  val color: String = "",
                  val clima: Int = 0)