package com.example.walkie.view

import android.app.Application
import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.util.TimeUnit
import android.location.Location
import android.location.LocationListener
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.walkie.R
import com.example.walkie.model.Walk
import com.example.walkie.viewmodel.StateViewModel
import com.example.walkie.viewmodel.UserViewModel
import com.google.android.gms.common.data.DataBufferObserver
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.model.Route
import com.maps.route.model.TravelMode
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.fragment_app_bar.*
import kotlin.math.roundToInt
import kotlin.random.Random


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private var difficultyLevel: Int = 2
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private lateinit var currentLatLng: LatLng
    private var disposable: Disposable?=null
    private lateinit var currentRoutePoints: Array<LatLng>
    private lateinit var currentVisitedPoints: Array<Boolean>
    private lateinit var viewModel: UserViewModel
    private var finalLength: Double = 0.0

    //test
    private lateinit var stateViewModel: StateViewModel
    //test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel= ViewModelProvider(this).get(UserViewModel::class.java)

        //test
        stateViewModel = ViewModelProvider(this).get(StateViewModel::class.java)
        //test
    }

    /**
     * Manipulates the map once available...
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpMap() {

        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            onBackPressed()
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location->
            if(location!=null){
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                generateRoute(currentLatLng)

                viewModel.walkViewModel.activeWalk.observe(this, Observer { activeWalk ->

                    start_walking_button.setOnClickListener {

                        viewModel.walkViewModel.addWalk(currentRoutePoints, 0.0)

                        locationRequest = LocationRequest().apply {
                            interval = SECONDS.toMillis(20)
                            fastestInterval = SECONDS.toMillis(10)
                            maxWaitTime = MINUTES.toMillis(1)

                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        }
                        locationCallback = object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult?) {
                                super.onLocationResult(locationResult)

                                if (locationResult?.lastLocation != null) {

                                    trackUserLocation(locationResult.lastLocation, activeWalk)
                                }
                            }
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                    }
                })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        setUpMap()

        reroll_route_button.setOnClickListener {
            mMap.clear()
            finalLength = 0.0
            if(locationCallback != null) fusedLocationClient?.removeLocationUpdates(locationCallback)
            visited_checkpoints_textView.text = "Visited checkpoints: 0/4"
            setUpMap()
        }

        topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

    }


    private fun generateRoute(initialLocation: LatLng){

        var xSign = 1
        var ySign = 1
        if(Random.nextBoolean()) xSign = -1
        if(Random.nextBoolean()) ySign = -1
        var xShift = 0.0
        var yShift = 0.0
        when(difficultyLevel) {
            1-> {
                xShift = (Random.nextFloat() - 0.5) / 2500 + 0.002 * xSign
                yShift = (Random.nextFloat() - 0.5) / 2500 + 0.002 * ySign
            }
            2-> {
                xShift = (Random.nextFloat() - 0.5) / 10000 + 0.012 * xSign
                yShift = (Random.nextFloat() - 0.5) / 10000 + 0.012 * ySign
            }
            3-> {
                xShift = (Random.nextFloat() - 0.5) / 15000 + 0.025 * xSign
                yShift = (Random.nextFloat() - 0.5) / 15000 + 0.025 * ySign
            }
        }
        val destination = LatLng(initialLocation.latitude+xShift, initialLocation.longitude+yShift)

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

        var middlePointList = arrayOfNulls<LatLng>(3)

        currentRoutePoints = Array(5){i->initialLocation}
        currentVisitedPoints = Array(5){i->false}
        currentRoutePoints[0] = initialLocation

        for(i in middlePointList.indices){

            var middlePointX = Random.nextDouble(xMin, xMax)
            var middlePointY = Random.nextDouble(yMin, yMax)

            middlePointList[i] = LatLng(middlePointX, middlePointY)
            currentRoutePoints[i+1] = middlePointList[i]!!
        }
        currentRoutePoints[4] = destination


        mMap.run{
            arrangeMarkers(middlePointList, initialLocation, destination, xMin, xMax, yMin, yMax)
        }
    }

    fun arrangeMarkers(middlePointList: Array<LatLng?>, initialLocation: LatLng, destination: LatLng, xMin: Double, xMax: Double, yMin: Double, yMax: Double){

        mMap.addMarker(MarkerOptions().position(initialLocation))
        mMap.addMarker(MarkerOptions().position(destination))

        for(point in middlePointList){
            mMap.addMarker(MarkerOptions().position(point!!))
        }

        mMap.addPolyline(PolylineOptions().add(initialLocation, middlePointList[0], middlePointList[1], middlePointList[2], destination, initialLocation).width(2.5f))

        var firstDistance = FloatArray(1)
        Location.distanceBetween(initialLocation.latitude, initialLocation.longitude, middlePointList[0]!!.latitude, middlePointList[0]!!.longitude, firstDistance)
        var secondDistance = FloatArray(1)
        Location.distanceBetween(middlePointList[0]!!.latitude, middlePointList[0]!!.longitude, middlePointList[1]!!.latitude, middlePointList[1]!!.longitude, secondDistance)
        var thirdDistance = FloatArray(1)
        Location.distanceBetween(middlePointList[1]!!.latitude, middlePointList[1]!!.longitude, middlePointList[2]!!.latitude, middlePointList[2]!!.longitude, thirdDistance)
        var fourthDistance = FloatArray(1)
        Location.distanceBetween(middlePointList[2]!!.latitude, middlePointList[2]!!.longitude, destination.latitude, destination.longitude, fourthDistance)
        var fifthDistance = FloatArray(1)
        Location.distanceBetween(destination.latitude, destination.longitude, initialLocation.latitude, initialLocation.longitude, fifthDistance)
        val totalDistance = firstDistance[0]+secondDistance[0]+thirdDistance[0]+fourthDistance[0]+fifthDistance[0]

        if((difficultyLevel==1 && (totalDistance<500 || totalDistance>1000)||(difficultyLevel==2 && (totalDistance<4500 || totalDistance>5000))||(difficultyLevel==3 && (totalDistance<9500 || totalDistance>10500)))){
            for(i in middlePointList.indices){

                var middlePointX = Random.nextDouble(xMin, xMax)
                var middlePointY = Random.nextDouble(yMin, yMax)

                middlePointList[i] = LatLng(middlePointX, middlePointY)
                currentRoutePoints[i+1] = middlePointList[i]!!
            }
            mMap.clear()
            arrangeMarkers(middlePointList, initialLocation, destination, xMin, xMax, yMin, yMax)
        }
        else{
            route_distance_textView.text = "Approximate length of the route: "+totalDistance+"\nDistance to destination: "+fifthDistance[0].toInt()+" meters"
        }
    }

    fun trackUserLocation(currentLocation: Location, activeWalk: Walk) {

        if(currentRoutePoints != null) {
            for (i in currentRoutePoints.indices) {
                if ((currentLocation.latitude < currentRoutePoints[i].latitude+0.0003 && currentLocation.latitude > currentRoutePoints[i].latitude-0.0003) && (currentLocation.longitude < currentRoutePoints[i].longitude+0.0003 && currentLocation.longitude > currentRoutePoints[i].longitude-0.0003)) {
                    if(currentVisitedPoints[i] == false) {
                        mMap.addMarker(
                                MarkerOptions().position(currentRoutePoints[i])
                                        .title("VISITED")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        )
                        currentVisitedPoints[i] = true
                        val vibrator = applicationContext?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        if (Build.VERSION.SDK_INT >= 26) {
                            vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                        }

                        viewModel.walkViewModel.activeWalk.value!!.visitedCheckpoints[i] = true
                    }
                }
            }
        }

        val dist = getMomentaryDistance(currentLocation)
        finalLength += dist
        stateViewModel.addDistanceTraveled(dist)

        viewModel.walkViewModel.activeWalk.value!!.length = finalLength
        viewModel.walkViewModel.updateWalk(activeWalk)

        var visitedPts = 0
        for(i in currentVisitedPoints){
            if(i == true) visitedPts++
        }

        visited_checkpoints_textView.text = "Visited checkpoints: "+(visitedPts-1)+"/4"
        route_distance_textView.text = "Traveled distance: "+finalLength.roundToInt()+" meters"


        if(currentVisitedPoints.all { visited -> visited }){
            viewModel.walkViewModel.completeWalk(activeWalk, finalLength)
            route_distance_textView.text = activeWalk.id.toString()
        }
        lastLocation = currentLocation
    }

    fun getMomentaryDistance(currentLocation: Location): Double{
        var R = 6378137
        var dLat = rad(lastLocation.latitude - currentLocation.latitude)
        var dLong = rad(lastLocation.longitude - currentLocation.longitude)
        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(rad(currentLocation.latitude)) * Math.cos(rad(lastLocation.latitude)) *
                Math.sin(dLong / 2) * Math.sin(dLong / 2)
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var d = R * c
        return d
    }

    fun rad(x: Double): Double{
        return x*Math.PI/180
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?) = false
}