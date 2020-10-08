package com.example.year3project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.contentcapture.DataShareWriteAdapter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class GetDataActivity : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.get_data)

        database = FirebaseFirestore.getInstance()
    }

    private fun getData(){
        database.collection("Users").get().addOnCompleteListener(object :OnCompleteListener<QuerySnapshot>{
            override fun onComplete(p0: Task<QuerySnapshot>) {
                var list = ArrayList<DatabaseModel>()
                if (p0.isSuccessful){
                    for (data in p0.result!!){
                        list.add(DatabaseModel(data.get("name") as String))
                    }
                }
            }

        })
    }
}