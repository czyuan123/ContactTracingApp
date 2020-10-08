package com.example.year3project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.store_data.*
import java.lang.Exception

class StoreDataActivity: AppCompatActivity() {
    private lateinit var database: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.store_data)

        database = FirebaseFirestore.getInstance()

        ButtonToSubmit.setOnClickListener {
            sendData()
        }

        /*ButtonToViewSubmission.setOnClickListener{
            startActivity(Intent(applicationContext, GetDataActivity::class.java))
        }*/

    }

    private fun sendData(){
        var name=editName.text.toString().trim()

        if (name.isNotEmpty()){
            var mode = DatabaseModel(name)
            database.collection("Users").add(mode)
                .addOnSuccessListener(object :OnSuccessListener<DocumentReference> {
                override fun onSuccess(p0: DocumentReference?) {
                    editName.setText("")
                }

            }).addOnFailureListener(object: OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(applicationContext, "failed", Toast.LENGTH_LONG).show()
                }

            })

        }
    }
}