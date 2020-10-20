package com.example.year3project

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlinx.android.synthetic.main.melaka.*
import kotlinx.android.synthetic.main.selangor.*
import kotlinx.android.synthetic.main.selangor.textActiveCase
import org.json.JSONArray
import org.json.JSONObject


class MelakaActivity: FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: FirebaseFirestore

    val red_district: MutableList<String> = ArrayList()
    val yellow_district: MutableList<String> = ArrayList()
    val green_district: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.melaka)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_melaka) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        val stateBound = LatLngBounds(
            LatLng(2.0611, 101.9071),
            LatLng(2.5112, 102.6143)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stateBound.center, 9.6f))

        database = FirebaseFirestore.getInstance()

        val selangor_districts = arrayOf("Alor Gajah", "Jasin", "Melaka Tengah")
        val town_map = java.util.HashMap<String, String?>()
        var totalInState = 0

        for (districts in selangor_districts) {

            val docRef = database.collection("Location").document("Melaka").collection(districts)
            docRef.get()
                .addOnSuccessListener { result ->
                    var stack_case = 0

                    if (districts == "Alor Gajah"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableAlorGajah, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textAlorGajah.text = "Alor Gajah: ${stack_case} active cases"
                        town_map.clear()
                        Log.d("addtable", "dis: ${districts}")
                    }

                    if (districts == "Jasin"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableJasin, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textJasin.text = "Jasin: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Melaka Tengah"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableMelakaTengah, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textMelakaTengah.text = "Melaka Tengah: ${stack_case} active cases"
                        town_map.clear()
                    }


                    textTotalInMelaka.text = totalInState.toString()
                    if (totalInState == 0){
                        textTotalInMelaka.setTextColor(Color.GREEN)
                        textActiveCaseMel.setTextColor((Color.GREEN))
                    }
                    else if (totalInState in 1..39){
                        textTotalInMelaka.setTextColor(Color.YELLOW)
                        textActiveCaseMel.setTextColor(Color.YELLOW)
                    }
                    else {
                        textTotalInMelaka.setTextColor(Color.RED)
                        textActiveCaseMel.setTextColor(Color.RED)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("errordb", "Error getting documents: ", exception)
                }
        }
    }

    private fun getJsonObjFromAsset(fileName: String): JSONObject? {
        try {
            val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
            return JSONObject(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addDataToTable(id: Int, townname: String, towncase: Any?) {
        val table = findViewById(id) as TableLayout
        val tbrow = TableRow(this)

        val t1v = TextView(this)
        t1v.text = townname
        t1v.setTextSize(18.0F)
        t1v.setTextColor(Color.GRAY)
        t1v.gravity = Gravity.CENTER
        t1v.setBackgroundResource(R.drawable.table_border)
        tbrow.addView(t1v)
        val t2v = TextView(this)
        t2v.text = towncase as CharSequence?
        t2v.setTextSize(18.0F)
        t2v.setTextColor(Color.GRAY)
        t2v.gravity = Gravity.CENTER
        t2v.setBackgroundResource(R.drawable.table_border)
        tbrow.addView(t2v)
        table.addView(tbrow)
    }

    private fun addZoneToArray(districts: String, stack_case: Int, googleMap: GoogleMap) {
        if (stack_case == 0) {
            green_district.add(districts)
        } else if (stack_case in 1..39) {
            yellow_district.add(districts)
        } else {
            red_district.add(districts)
        }
        colourDistricts(googleMap)
    }

    private fun colourDistricts(googleMap: GoogleMap){
        val f = assets.list("")
        if (f != null) {
            for (fn in f) {
                var jsonfile = fn.dropLast(5)

                if (jsonfile in yellow_district){
                    val geoJsonData: JSONObject? = getJsonObjFromAsset(jsonfile + ".json")
                    val layer = GeoJsonLayer(googleMap, geoJsonData)
                    val style = layer.defaultPolygonStyle
                    style.strokeWidth = 1f
                    style.fillColor = Color.YELLOW
                    style.strokeColor = Color.YELLOW
                    layer.addLayerToMap()
                }
                else if (jsonfile in red_district){
                    val geoJsonData: JSONObject? = getJsonObjFromAsset(jsonfile + ".json")
                    val layer = GeoJsonLayer(googleMap, geoJsonData)
                    val style = layer.defaultPolygonStyle
                    style.strokeWidth = 1f
                    style.fillColor = Color.RED
                    style.strokeColor = Color.RED
                    layer.addLayerToMap()
                }
                else if (jsonfile in green_district){
                    val geoJsonData: JSONObject? = getJsonObjFromAsset(jsonfile + ".json")
                    val layer = GeoJsonLayer(googleMap, geoJsonData)
                    val style = layer.defaultPolygonStyle
                    style.strokeWidth = 1f
                    style.fillColor = Color.GREEN
                    style.strokeColor = Color.GREEN
                    layer.addLayerToMap()
                }
            }
        }
    }
}