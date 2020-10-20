package com.example.year3project

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import java.util.*
import kotlin.concurrent.schedule


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    lateinit var lm : LocationManager
    private lateinit var database: FirebaseFirestore

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
        setContentView(R.layout.maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        database = FirebaseFirestore.getInstance()

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED //1
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 111)

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var loclisten = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                reverseGeocode(location)
            }
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100.2f, loclisten)
    }

    override fun onStart() {
        super.onStart()
        val docRef = database.collection("User").document("Zone")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("exist", "DocumentSnapshot data: ${document.data}")

                } else {
                    Log.d("dontexist", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("errordb", "get failed with ", exception)
            }
    }

    private fun reverseGeocode(loc: Location) { //1
        var geoc = Geocoder(this, Locale.getDefault())
        var addresses = geoc.getFromLocation(loc.latitude, loc.longitude, 2)
        var address = addresses.get(0)
        var postcode = address.postalCode
        var city = address.locality
        var citycheck = 0
        var redcheck = 0
        var yellowcheck = 0
        val latLng = LatLng(loc.latitude, loc.longitude)
        val loc: MutableMap<String, Any> = HashMap()
        loc["Postal Code"] = postcode
        loc["City"] = city

        database.collection("User").document("Location").set(loc)

        val docRefUser = database.collection("State").document("Selangor")
        docRefUser.get()
            .addOnSuccessListener { document ->
                Log.d("hihi", "${document.data}")
                val markerOptions = MarkerOptions().position(latLng)
                if (document != null) {
                    val jsonData = getJsonDataFromAsset("city_cases.json")
                    jsonData?.let {
                        for (i in 0 until it.length()) {
                            val entry = it.getJSONObject(i)
                            val cases = entry.getInt("Cases")
                            val xcity = entry.getString("City")

                            if (city == xcity){
                                if (cases in 1..39){
                                    yellowcheck = 1
                                    if (yellowcheck == 1){
                                        mMap.addMarker((markerOptions).title("Yellow Zone").snippet("${cases} active cases in ${xcity}").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                        yellowcheck = 0
                                        citycheck++
                                        Timer().schedule(2000) {
                                            YellowNotificationActivity().show(supportFragmentManager, "MyCustomFragment")
                                        }
                                    }
                                }
                                else if (cases >= 40){
                                    redcheck = 1
                                    if (redcheck == 1){
                                        mMap.addMarker((markerOptions).title("Red Zone").snippet("${cases} active cases in ${xcity}").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                        redcheck = 0
                                        citycheck++
                                        Timer().schedule(2000) {
                                            RedNotificationActivity().show(supportFragmentManager, "MyCustomFragment")
                                        }
                                    }
                                }
                            }
                        }
                    }


                } else {
                    Log.d("dontexist", "No such document")
                }
                if (citycheck == 0){
                    mMap.addMarker((markerOptions).title("Green Zone").snippet("No active cases in ${city}").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
            .addOnFailureListener { exception ->
                Log.d("errordb", "get failed with ", exception)
            }

        mMap.clear()
        citycheck = 0

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationAccess()
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

}
