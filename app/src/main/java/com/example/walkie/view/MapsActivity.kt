package com.example.walkie.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.walkie.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.random.Random

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val xCord = 31.490127
        val yCord = 74.316971

        val initialLocation = LatLng(xCord, yCord)

        val xShift = (Random.nextFloat()-0.5)/10
        val yShift = (Random.nextFloat()-0.5)/10

        val destination = LatLng(xCord+xShift, yCord+yShift)

        var xMin = 0.0
        var xMax = 0.0
        var yMin = 0.0
        var yMax = 0.0

        if(initialLocation.latitude>destination.latitude) {
            xMax = initialLocation.latitude
            xMin = destination.latitude
        }
        else{
            xMin = initialLocation.latitude
            xMax = destination.latitude
        }

        if(initialLocation.longitude>destination.longitude){
            yMax = initialLocation.longitude
            yMin = destination.longitude
        }
        else{
            yMin = initialLocation.longitude
            yMax = destination.longitude
        }

        var middlePointList = arrayOfNulls<LatLng>(4)

        for(i in middlePointList.indices){

            var middlePointX = Random.nextDouble(xMin, xMax)
            var middlePointY = Random.nextDouble(yMin, yMax)

            middlePointList[i] = LatLng(middlePointX, middlePointY)
        }


        googleMap.run{
            val zoom = 15.0f
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, zoom))

            mMap.addMarker(MarkerOptions().position(initialLocation))
            mMap.addMarker(MarkerOptions().position(destination))

            for(point in middlePointList){
                mMap.addMarker(MarkerOptions().position(point!!))
            }


        }

        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}