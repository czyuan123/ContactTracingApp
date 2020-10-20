package com.example.year3project


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlinx.android.synthetic.main.zones.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.concurrent.schedule


class ZonesActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var lm : LocationManager
    private lateinit var database: FirebaseFirestore

    val red_district: MutableList<String> = ArrayList()
    val yellow_district: MutableList<String> = ArrayList()
    val green_district: MutableList<String> = ArrayList()

    private fun getLocationAccess(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
        }
        else
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.zones)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        database = FirebaseFirestore.getInstance()

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED //1
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 111)

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        ButtonToMyLocation.setOnClickListener {
            var loclisten = object: LocationListener {
                override fun onLocationChanged(location: Location) {
                    mMap.clear()
                    reverseGeocode(location)
                }
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100.2f, loclisten)

        }

        ButtonToOtherLocation.setOnClickListener {
            val listStates = arrayOf("Selangor", "Negeri Sembilan", "Melaka", "Johor")
            val selectStateBuilder = AlertDialog.Builder(this)
            selectStateBuilder.setTitle("Select State")
            selectStateBuilder.setSingleChoiceItems(listStates, -1){
                    dialog, i ->
                Log.d("choice", "${i}")
                dialog!!.dismiss()

                if (i == 0){
                    startActivity(Intent(this, SelangorActivity::class.java))
                }

                else if (i == 2){
                    startActivity(Intent(this, MelakaActivity::class.java))
                }

            }
            selectStateBuilder.setNeutralButton("Cancel"){
                    dialog: DialogInterface?, which: Int ->
                dialog!!.cancel()
            }
            val stateDialog = selectStateBuilder.create()
            stateDialog.show()
        }

    }

    private fun reverseGeocode(loc: Location) {

        var geoc = Geocoder(this, Locale.getDefault())
        var addresses = geoc.getFromLocation(loc.latitude, loc.longitude, 2)
        var address = addresses.get(0)
        var town = address.locality
        val latLng = LatLng(loc.latitude, loc.longitude)
        val loc: MutableMap<String, Any> = java.util.HashMap()
        loc["Town"] = town
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        loc["Timestamp"] = currentDate

        database.collection("Users").document("User ID").collection("Location").add(loc)
        val markerOptions = MarkerOptions().position(latLng)
        val malaysia = HashMap<String, ArrayList<String>>()
        malaysia["Selangor"] = arrayListOf("Sabak Bernam", "Kuala Selangor","Klang","Sepang","Hulu Langat","Hulu Selangor","Gombak","Petaling","Kuala Langat")
        malaysia["Melaka"] = arrayListOf("Alor Gajah","Jasin","Melaka Tengah")
        val town_map = HashMap<String, String?>()
        var checkloc = 0
        val user_loc = town
        var num_cases = 0

        for (state in malaysia.keys) {
            for (district in malaysia["${state}"]!!) {
                val docRef = database.collection("Location").document(state).collection(district)
                docRef.get()
                    .addOnSuccessListener { result ->

                        for (document in result) {
                            town_map["${document.id}"] = "${document.get("Cases")}"
                        }

                        if (user_loc in town_map.keys) {
                            num_cases = town_map[user_loc]!!.toInt()
                            checkloc = 111
                            if (checkloc == 111) {
                                Log.d("readfirebase", "city in doc ")
                                if (num_cases == 0) {
                                    Log.d("readfirebase", "green zone")
                                    mMap.addMarker((markerOptions).title("GREEN Zone").snippet("${num_cases} active case(s) in ${user_loc}").icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_GREEN)))
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                } else if (num_cases in 1..39) {
                                    Log.d("readfirebase", "yellow zone")
                                    mMap.addMarker((markerOptions).title("Yellow Zone").snippet("${num_cases} active case(s) in ${user_loc}").icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_YELLOW)))
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                    Timer().schedule(2000) {
                                        YellowNotificationActivity().show(supportFragmentManager, "MyCustomFragment")
                                    }
                                } else {
                                    Log.d("readfirebase", "red zone")
                                    mMap.addMarker((markerOptions).title("Red Zone").snippet("${num_cases} active case(s) in ${user_loc}").icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED)))
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                    Timer().schedule(2000) {
                                        RedNotificationActivity().show(supportFragmentManager, "MyCustomFragment")
                                    }
                                }
                            }
                        } else {
                            checkloc++
                            if (checkloc == 9) {
                                Log.d("readfirebase", "city not in doc ")
                                Log.d("readfirebase", "green zone")
                                mMap.addMarker((markerOptions).title("GREEN Zone").snippet("${num_cases} active cases in ${user_loc}").icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN)))
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            }
                        }
                        town_map.clear()
                    }
                    .addOnFailureListener { exception ->
                        Log.d("errordb", "Error getting documents: ", exception)
                    }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        val msiaBounds = LatLngBounds(
            LatLng(0.773131415201, 100.085756871),
            LatLng(6.92805288332, 119.181903925)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(msiaBounds.center, 4.8f))
        calcCaseInDistrict(mMap)
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

    private fun calcCaseInDistrict(googleMap: GoogleMap){
        val malaysia = HashMap<String, ArrayList<String>>()
        malaysia["Selangor"] = arrayListOf("Sabak Bernam", "Kuala Selangor","Klang","Sepang","Hulu Langat","Hulu Selangor","Gombak","Petaling","Kuala Langat")
        malaysia["Melaka"] = arrayListOf("Alor Gajah","Jasin","Melaka Tengah")
        val town_map = HashMap<String, String?>()

        for (state in malaysia.keys) {
            for (district in malaysia["${state}"]!!) {
                val docRef = database.collection("Location").document(state).collection(district)
                docRef.get()
                    .addOnSuccessListener { result ->
                        var stack_case = 0
                        for (document in result) {
                            town_map["${document.id}"] = "${document.get("Cases")}"
                            var add_case = "${document.get("Cases")}".toInt()
                            if (add_case != null) {
                                stack_case += add_case
                            }
                        }
                        Log.d("calccase", "district: ${district}, cases: ${stack_case}")
                        addZoneToArray(district, stack_case, googleMap)
                    }
            }
        }

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