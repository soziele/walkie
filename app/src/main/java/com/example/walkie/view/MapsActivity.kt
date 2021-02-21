package com.example.walkie.view

import android.app.Application
import android.content.ContentProvider
import android.content.Context
import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.walkie.R
import com.example.walkie.model.Walk
import com.example.walkie.model.enums.Difficulty
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.random.Random


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private lateinit var currentLatLng: LatLng
    private var disposable: Disposable? = null
    private lateinit var currentRoutePoints: Array<LatLng>
    private lateinit var currentVisitedPoints: Array<Boolean>
    private lateinit var viewModel: UserViewModel
    private var estimatedLength: Double = 0.0
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
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)

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
    private fun setUpMap(isNew: Boolean) {

        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                if(isNew) {
                    generateRoute(currentLatLng)
                }
                start_walking_button.setOnClickListener {

                    if(isNew) {
                        runBlocking {
                            viewModel.walkViewModel.addWalk(currentRoutePoints, estimatedLength)
                            viewModel.walkViewModel.cancelWalk(viewModel.walkViewModel.activeWalk)

                            viewModel.walkViewModel.getActiveWalk()
                        }
                    }

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

                                trackUserLocation(locationResult.lastLocation, viewModel.walkViewModel.activeWalk)
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                    visited_checkpoints_textView.text = viewModel.walkViewModel.activeWalk.id.toString()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

        /*if (viewModel.walkViewModel.hasCompletedWalkToday()) {
            this.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle("You have already finished one walk today.")
                        .setMessage("Congratulations, leave something for the other day.")
                        .setPositiveButton("OK",DialogInterface.OnClickListener { dialog, id ->
                            onBackPressed()
                        })

                builder.create()
                builder.show()
            }
        } else */if (viewModel.walkViewModel.activeWalk != null) {
            this.let {
                val builder = AlertDialog.Builder(it)
                builder.setTitle("You have an unfinished walk!")
                        .setMessage("Do you want to restore it?")
                        .setPositiveButton("YES",DialogInterface.OnClickListener { dialog, id ->
                            restoreRoute(viewModel.walkViewModel.activeWalk)
                        })
                        .setNegativeButton("NO", DialogInterface.OnClickListener { dialog, id ->
                            setUpMap(isNew = true)
                        })
                builder.create()
                builder.show()
            }
        }

        else {
            setUpMap(isNew = true)
        }
        reroll_route_button.setOnClickListener {
            mMap.clear()
            finalLength = 0.0
            estimatedLength = 0.0
            if (locationCallback != null) fusedLocationClient?.removeLocationUpdates(locationCallback)
            visited_checkpoints_textView.text = "Visited checkpoints: 0/5"
            setUpMap(isNew = true)
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
        when(stateViewModel.getState().difficulty.ordinal) {
            0-> {
                xShift = (Random.nextFloat() - 0.5) / 1250 + 0.004 * xSign
                yShift = (Random.nextFloat() - 0.5) / 1250 + 0.004 * ySign
            }
            1-> {
                xShift = (Random.nextFloat() - 0.5) / 10000 + 0.012 * xSign
                yShift = (Random.nextFloat() - 0.5) / 10000 + 0.012 * ySign
            }
            2-> {
                xShift = (Random.nextFloat() - 0.5) / 5000 + 0.035 * xSign
                yShift = (Random.nextFloat() - 0.5) / 5000 + 0.035* ySign
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

            if(stateViewModel.getState().difficulty == Difficulty.Hard) middlePointList[i] = LatLng(middlePointX-0.0001, middlePointY+0.0001)
            else middlePointList[i] = LatLng(middlePointX, middlePointY)
            currentRoutePoints[i+1] = middlePointList[i]!!
        }
        currentRoutePoints[4] = destination


        mMap.run{
            arrangeMarkers(middlePointList, initialLocation, destination, xMin, xMax, yMin, yMax)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun restoreRoute(walk: Walk){
        for(i in walk.checkpoints.indices){
            if(walk.visitedCheckpoints[i]){
                mMap.addMarker(
                        MarkerOptions().position(walk.checkpoints[i])
                                .title("VISITED")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
            }
            else{
                mMap.addMarker(MarkerOptions().position(walk.checkpoints[i]))
            }
        }
        //mMap.addPolyline(PolylineOptions().add(walk.checkpoints[0], walk.checkpoints[1], walk.checkpoints[2], walk.checkpoints[3], walk.checkpoints[4], walk.checkpoints[0]).width(2.5f))
        currentRoutePoints = walk.checkpoints
        currentVisitedPoints = walk.visitedCheckpoints.toTypedArray()
        connectPoints()


        finalLength = walk.distanceTraveled

        route_distance_textView.text = "Approximate length of the route: "+walk.length.toInt()+" meters\nYou've already walked "+walk.distanceTraveled.toInt()+" meters"
        var visitedNumber = 0
        for(visited in walk.visitedCheckpoints){
            if(visited) visitedNumber++
        }
        visited_checkpoints_textView.text = "Visited checkpoints: "+visitedNumber+"/5"

        setUpMap(isNew = false)
    }

    fun arrangeMarkers(middlePointList: Array<LatLng?>, initialLocation: LatLng, destination: LatLng, xMin: Double, xMax: Double, yMin: Double, yMax: Double){

        mMap.addMarker(MarkerOptions().position(initialLocation))
        mMap.addMarker(MarkerOptions().position(destination))

        for(point in middlePointList){
            mMap.addMarker(MarkerOptions().position(point!!))
        }

        connectPoints()

        if((stateViewModel.getState().difficulty.ordinal==0 && (estimatedLength<1000 || estimatedLength>1300)) ||(stateViewModel.getState().difficulty.ordinal==1 && (estimatedLength<3500 || estimatedLength>4000)) || (stateViewModel.getState().difficulty.ordinal==2 && (estimatedLength<10000 || estimatedLength>11000))){
            for(i in middlePointList.indices){

                var middlePointX = Random.nextDouble(xMin, xMax)
                var middlePointY = Random.nextDouble(yMin, yMax)

                middlePointList[i] = LatLng(middlePointX, middlePointY)
                if(stateViewModel.getState().difficulty == Difficulty.Hard) middlePointList[i] = LatLng(middlePointX-0.0001, middlePointY+0.0001)
                currentRoutePoints[i+1] = middlePointList[i]!!
            }
            mMap.clear()
            estimatedLength = 0.0
            arrangeMarkers(middlePointList, initialLocation, destination, xMin, xMax, yMin, yMax)
        }
        else{
            route_distance_textView.text = "Approximate length of the route: "+estimatedLength.toInt()
        }
    }

    fun trackUserLocation(currentLocation: Location, activeWalk: Walk) {

        val dist = getMomentaryDistance(currentLocation)
        finalLength += dist

        if(currentRoutePoints != null) {
            for (i in currentRoutePoints.indices) {
                if ((currentLocation.latitude < currentRoutePoints[i].latitude+0.0003 && currentLocation.latitude > currentRoutePoints[i].latitude-0.0003) && (currentLocation.longitude < currentRoutePoints[i].longitude+0.0003 && currentLocation.longitude > currentRoutePoints[i].longitude-0.0003)) {
                    if(currentVisitedPoints[i] == false) {
                        if (i != 0 || (currentVisitedPoints[1] && currentVisitedPoints[2] && currentVisitedPoints[3] && currentVisitedPoints[4])) {
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

                            viewModel.walkViewModel.activeWalk.visitedCheckpoints[i] = true
                            viewModel.walkViewModel.activeWalk.distanceTraveled = finalLength
                            viewModel.walkViewModel.updateWalk(viewModel.walkViewModel.activeWalk)
                            stateViewModel.addDistanceAndCheckpoint(finalLength)
                        }
                    }
                }
            }
        }

        var visitedPts = 0
        for(i in currentVisitedPoints){
            if(i == true) visitedPts++
        }

        visited_checkpoints_textView.text = "Visited checkpoints: "+visitedPts+"/5"
        route_distance_textView.text = "Traveled distance: "+finalLength.roundToInt()+" meters"


        if(currentVisitedPoints.all { visited -> visited }){
            viewModel.walkViewModel.completeWalk(viewModel.walkViewModel.activeWalk, finalLength)
            route_distance_textView.text = viewModel.walkViewModel.activeWalk.id.toString()
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

    fun connectPoints(){
        var isConnectedFromFront = arrayOf(false, false, false, false, false)
        var isConnectedFromBehind = arrayOf(false, false, false, false, false)

        var currentlyConnectedId = 0
        for(i in currentRoutePoints.indices){

            var connectedId = findNearestNeighbor(currentlyConnectedId, isConnectedFromBehind, isConnectedFromFront)
            if(connectedId != -1) {
                isConnectedFromFront[currentlyConnectedId] = true
                isConnectedFromBehind[connectedId] = true
                mMap.addPolyline(PolylineOptions().add(currentRoutePoints[currentlyConnectedId], currentRoutePoints[connectedId]).width(2f))
                currentlyConnectedId = connectedId
            }
        }

    }

    fun findNearestNeighbor(id: Int, isConnectedFromBehind: Array<Boolean>, isConnectedFromFront: Array<Boolean>): Int{

        var nearestNeighborId = -1
        var tmpDistance = FloatArray(1)
        var smallestDistance = 10000.0

        var connectedNumber = 0
        for(i in isConnectedFromFront){
            if(i) connectedNumber++
        }

        for(i in currentRoutePoints.indices){
            if(i != id && !isConnectedFromFront[id] && !isConnectedFromBehind[i] && !(i == 0 && connectedNumber < 4)){
                Location.distanceBetween(currentRoutePoints[id].latitude, currentRoutePoints[id].longitude, currentRoutePoints[i].latitude, currentRoutePoints[i].longitude, tmpDistance)

                if(tmpDistance[0] < smallestDistance){
                    smallestDistance = tmpDistance[0].toDouble()
                    nearestNeighborId = i
                }
            }
        }
        estimatedLength+=smallestDistance
        return nearestNeighborId
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?) = false
}