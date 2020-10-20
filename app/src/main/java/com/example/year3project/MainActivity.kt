package com.example.year3project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButtonToLocation.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        ButtonToStoreData.setOnClickListener {
            startActivity(Intent(this, StoreDataActivity::class.java))
        }

        ButtonToMaps.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        ButtonToHeatmap.setOnClickListener {
            startActivity(Intent(this, HeatmapActivity::class.java))
        }

        ButtonToReadCity.setOnClickListener {
            startActivity(Intent(this, ReadCityActivity::class.java))
        }


    }
}