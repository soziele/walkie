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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.walkie.R
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
import kotlin.random.Random


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLatLng: LatLng
    private var disposable: Disposable?=null
    private lateinit var currentRoutePoints: Array<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this){location->
            if(location!=null){
                lastLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                generateRoute(currentLatLng)

                start_walking_button.setOnClickListener {
                    locationRequest = LocationRequest().apply {
                        interval = SECONDS.toMillis(20)
                        fastestInterval = SECONDS.toMillis(10)
                        maxWaitTime = MINUTES.toMillis(1)

                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    }
                    locationCallback = object: LocationCallback(){
                        override fun onLocationResult(locationResult: LocationResult?) {
                            super.onLocationResult(locationResult)

                            if (locationResult?.lastLocation != null) {
                                lastLocation = locationResult.lastLocation
                                trackUserLocation()
                            }
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()

        reroll_route_button.setOnClickListener {
            mMap.clear()
            setUpMap()
        }

    }

    private fun generateRoute(initialLocation: LatLng){

        val xShift = (Random.nextFloat()-0.5)/50
        val yShift = xShift

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
        currentRoutePoints[0] = initialLocation

        for(i in middlePointList.indices){

            var middlePointX = Random.nextDouble(xMin, xMax)
            var middlePointY = Random.nextDouble(yMin, yMax)

            middlePointList[i] = LatLng(middlePointX, middlePointY)
            currentRoutePoints[i+1] = middlePointList[i]!!
        }
        currentRoutePoints[4] = destination


        mMap.run{

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

            route_distance_textView.text = "Length from initial location to destination: "+firstDistance[0]+"\nApproximate length of the route: "+totalDistance
        }
    }

    fun trackUserLocation() {

        if(currentRoutePoints != null) {
            for (i in currentRoutePoints.indices) {
                if ((lastLocation.latitude < currentRoutePoints[i].latitude+0.00001 && lastLocation.latitude > currentRoutePoints[i].latitude-0.00001) && (lastLocation.longitude < currentRoutePoints[i].longitude+0.00001 && lastLocation.longitude > currentRoutePoints[i].longitude-0.00001)) {
                    mMap.addMarker(
                        MarkerOptions().position(currentRoutePoints[i])
                            .title("VISITED")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    val vibrator = applicationContext?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            }
        }
        visited_checkpoints_textView.text = "Current location: ["+lastLocation.latitude+" , "+lastLocation.longitude+"]"
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?) = false
}