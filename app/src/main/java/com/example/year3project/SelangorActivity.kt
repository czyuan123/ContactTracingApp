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
import kotlinx.android.synthetic.main.selangor.*
import org.json.JSONArray
import org.json.JSONObject


class SelangorActivity: FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: FirebaseFirestore

    val red_district: MutableList<String> = ArrayList()
    val yellow_district: MutableList<String> = ArrayList()
    val green_district: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selangor)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_selangor) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        val stateBound = LatLngBounds(
            LatLng(2.4930, 100.7448),
            LatLng(3.8806, 102.0357)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(stateBound.center, 8f))

        database = FirebaseFirestore.getInstance()

        val selangor_districts = arrayOf("Sabak Bernam", "Kuala Selangor","Klang","Sepang","Hulu Langat","Hulu Selangor","Gombak","Petaling","Kuala Langat")
        val town_map = java.util.HashMap<String, String?>()
        var totalInState = 0

        for (districts in selangor_districts) {

            val docRef = database.collection("Location").document("Selangor").collection(districts)
            docRef.get()
                .addOnSuccessListener { result ->
                    var stack_case = 0

                    if (districts == "Sabak Bernam"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableSabakBernam, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        addZoneToArray(districts, stack_case, googleMap)
                        textSabakBernam.text = "Sabak Bernam: ${stack_case} active cases"
                        town_map.clear()
                        Log.d("addtable", "dis: ${districts}")
                    }

                    if (districts == "Kuala Selangor"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableKualaSelangor, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textKualaSelangor.text = "Kuala Selangor: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Klang"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableKlang, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textKlang.text = "Klang: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Sepang"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableSepang, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textSepang.text = "Sepang: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Hulu Langat"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableHuluLangat, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textHuluLangat.text = "Hulu Langat: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Hulu Selangor"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableHuluSelangor, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textHuluSelangor.text = "Hulu Selangor: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Gombak"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableGombak, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textGombak.text = "Gombak: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Petaling"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tablePetaling, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textPetaling.text = "Petaling: ${stack_case} active cases"
                        town_map.clear()
                    }

                    if (districts == "Kuala Langat"){
                        for (town in result) {
                            town_map[town.id] = "${town.get("Cases")}"
                        }

                        for (data in town_map){
                            addDataToTable(R.id.tableKualaLangat, data.key, data.value)
                            val add_case = (data.value)?.toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        totalInState += stack_case
                        addZoneToArray(districts, stack_case, googleMap)
                        textKualaLangat.text = "Kuala Langat: ${totalInState} active cases"
                        town_map.clear()
                    }

                    textTotalInSelangor.text = totalInState.toString()
                    if (totalInState == 0){
                        textTotalInSelangor.setTextColor(Color.GREEN)
                        textActiveCase.setTextColor((Color.GREEN))
                    }
                    else if (totalInState in 1..39){
                        textTotalInSelangor.setTextColor(Color.YELLOW)
                        textActiveCase.setTextColor(Color.YELLOW)
                    }
                    else {
                        textTotalInSelangor.setTextColor(Color.RED)
                        textActiveCase.setTextColor(Color.RED)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("errordb", "Error getting documents: ", exception)
                }
        }



        Log.d("addtable", "greendis: ${green_district}")
    }

    private fun getJsonDataFromAsset(fileName: String): JSONArray? {
        try {
            val jsonString = assets.open(fileName).bufferedReader().use { it.readText() }
            return JSONArray(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
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