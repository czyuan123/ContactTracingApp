package com.example.year3project

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.location.*
import java.util.*

class LocationActivity: AppCompatActivity() {

    lateinit var lm : LocationManager
    //lateinit var l : Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 111)

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!

        var loclisten = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                reverseGeocode(location)
            }
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100.2f, loclisten)
    }
    private fun reverseGeocode(loc: Location) {
        var geoc = Geocoder(this, Locale.getDefault())
        var addresses = geoc.getFromLocation(loc.latitude, loc.longitude, 2)
        var address = addresses.get(0)
        textViewLocation.setText("Current location is \n${address.getAddressLine(0)}\n${address.locality}")
        //textViewLocation.setText("Current location is \n${address.postalCode}")
    }
}