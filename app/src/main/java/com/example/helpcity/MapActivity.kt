package com.example.helpcity

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.helpcity.api.EndPoints
import com.example.helpcity.api.Occurrence
import com.example.helpcity.api.ServiceBuilder
import com.example.helpcity.geofence.GeofenceHelper
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_map.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.URL
import kotlin.math.roundToInt

const val LOCATION_PERMISSION_REQUEST_CODE = 1000
const val FINE_LOCATION_ACCESS_REQUEST_CODE = 10002
const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
const val GEOFENCE_ID = "SOME_GEOFENCE_ID"

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    SensorEventListener {

    private var map: GoogleMap? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var occurrences: List<Occurrence>
    private lateinit var markers: ArrayList<Marker>

    //Geofencing
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geoFenceHelper: GeofenceHelper
    private var range: Float = 200f
    private var notifications: Boolean = false

    // Sensors
    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null
    private var informationText: String = ""
    private lateinit var compass: ImageView
    private lateinit var orientationView: TextView
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private var lastAccelerometer: FloatArray = FloatArray(3)
    private var lastMagnetometer: FloatArray = FloatArray(3)
    private var orientation: FloatArray = FloatArray(3)
    private var rotationMatrix: FloatArray = FloatArray(9)
    private lateinit var info_btn: ImageView
    private lateinit var popupWindow: PopupWindow
    private lateinit var info_txt: TextView
    private var isLastAccelerometerArrayCopied = false
    private var isLastMagnetometerArrayCopied = false
    private var lastUpdatedTime: Long = 0
    private var currentDegree = 0f
    private val TAG = MapActivity::class.java.simpleName

    // HashMap para Ligar cada Marker ao seu Type de modo a ser mais fácil filtrar
    private lateinit var markersTypeHashMap: HashMap<Marker, String>

    // animation
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    private var clicked = false

    // last know location
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // location periodic updates
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initialize the fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // update location
        createLocationRequest()

        // Geofecing
        geofencingClient = LocationServices.getGeofencingClient(this)
        geoFenceHelper = GeofenceHelper(this)
        notifications = AppPreferences.notifications
        range = AppPreferences.radius

        // Sensors
        compass = this.findViewById(R.id.compass)
        orientationView = this.findViewById(R.id.orientation)
        initializeSensors()

        // location periodic updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.3f)) // Follow me option
            }
        }

        continuousSlider.addOnChangeListener(Slider.OnChangeListener { slider, _, _ ->
            if (markers.isNotEmpty()) {
                filterByDistance(lastLocation, markers, slider.value.roundToInt())
            }

        })
        continuousSlider.setLabelFormatter { value: Float ->
            val nomeAntesDoSlider = resources.getString(R.string.range_of)
            val nomeMetros = resources.getString(R.string.meters)
            return@setLabelFormatter nomeAntesDoSlider.plus(" ${value.roundToInt()} ")
                .plus(nomeMetros)
        }

        getAllOccurrences()

        // Ir para a Atividade de Criar novas Ocorrencias
        _occurrenceFab.setOnClickListener {
            occurrenceFabClicked()
        }

        new_occurrenceFab.setOnClickListener {
            val i = Intent(this, NewOccurrenceActivity::class.java)
            startActivity(i)
        }

        list_occurrenceFab.setOnClickListener {
            val i = Intent(this, OccurrenceActivity::class.java)
            startActivity(i)
        }
    }

    private fun initializeSensors() {
        sensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    private fun brightness(brightness: Float) {
        map?.let { setMapStyle(it, brightness) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val light = event.values[0]
            brightness(light)
        } else if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            isLastAccelerometerArrayCopied = true
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            isLastMagnetometerArrayCopied = true
        }

        if (isLastAccelerometerArrayCopied && isLastMagnetometerArrayCopied && (System.currentTimeMillis() - lastUpdatedTime > 250)) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
            SensorManager.getOrientation(rotationMatrix, orientation)

            var azimuthInRadians = orientation[0].toDouble()
            var azimuthInDegree = Math.toDegrees(azimuthInRadians).toFloat()

            var rotateAnimation = RotateAnimation(
                currentDegree,
                -azimuthInDegree,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )

            rotateAnimation.duration = 250
            rotateAnimation.fillAfter = true
            compass.startAnimation(rotateAnimation)
            orientationView.text = azimuthInDegree.toInt().toString() + "°"
            currentDegree = -azimuthInDegree
            lastUpdatedTime = System.currentTimeMillis()

        }

    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            )
            return
        }
    }

    private fun getAllOccurrences() {
        // Call the service
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getOccurrences()

        call.enqueue(object : Callback<List<Occurrence>> {
            override fun onResponse(
                call: Call<List<Occurrence>>,
                response: Response<List<Occurrence>>
            ) {

                markers = ArrayList()
                markersTypeHashMap = HashMap()

                if (response.body() != null) {

                    occurrences = response.body()!!

                    // https://stackoverflow.com/questions/6343166/how-to-fix-android-os-networkonmainthreadexception
                    // android.os.NetworkOnMainThreadException
                    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)

                    for (occurrence in occurrences) {


                        // Creates Bitmap objects from various sources, including files, streams, and byte-arrays.
                        // https://developer.android.com/reference/kotlin/android/graphics/BitmapFactory
                        try {
                            val urlImage =
                                "http://helpcity.000webhostapp.com/uploads/" + occurrence.image
                            var input: InputStream = URL(urlImage).openStream()
                            var bitmap = BitmapFactory.decodeStream(input)
                            var icon: BitmapDescriptor =
                                BitmapDescriptorFactory.fromBitmap(bitmap)

                            var position =
                                LatLng(occurrence.lat.toDouble(), occurrence.lng.toDouble())

                            val marker: Marker = map!!.addMarker(
                                MarkerOptions().position(position).title(occurrence.type)
                                    .snippet(occurrence.description).icon(icon)
                            )

                            markers.add(marker) // Adiciona-mos ao nosso ArrayList para mais tarde conseguirmos mexer nela e fazer o que pretendemos
                            markersTypeHashMap[marker] = occurrence.type

                        } catch (e: Exception) {
                            Log.e("HELDER", e.toString())

                            var position =
                                LatLng(occurrence.lat.toDouble(), occurrence.lng.toDouble())

                            val marker: Marker = map!!.addMarker(
                                MarkerOptions().position(position).title(occurrence.type)
                                    .snippet(occurrence.description)
                            )

                            markers.add(marker) // Adiciona-mos ao nosso ArrayList para mais tarde conseguirmos mexer nela e fazer o que pretendemos
                            markersTypeHashMap[marker] = occurrence.type
                        }
                    }
                } else {
                    markers.clear()
                }
            }

            override fun onFailure(call: Call<List<Occurrence>>, t: Throwable) {
                Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setMapStyle(map: GoogleMap, brightness: Float) {
        if (brightness.toInt() > 5000) {
            try {
                // Customize the styling of the base map using a JSON object defined
                // in a raw resource file.
                val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.map_style_normal
                    )
                )

                if (!success) {
                    Log.e(TAG, "Style parsing failed.")
                }
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "Can't find style. Error: ", e)
            }
        } else {
            try {
                // Customize the styling of the base map using a JSON object defined
                // in a raw resource file.
                val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.map_style
                    )
                )

                if (!success) {
                    Log.e(TAG, "Style parsing failed.")
                }
            } catch (e: Resources.NotFoundException) {
                Log.e(TAG, "Can't find style. Error: ", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        requestForegroundAndBackgroundLocationPermissions()
        if (notifications) {
            map!!.setOnMapLongClickListener(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            return
        } else {
            map!!.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    // Initializes contents of Activity's standard options menu. Only called the first time options
    // menu is displayed.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    // Called whenever an item in your options menu is selected.
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.type_filter -> {
            openDialogType()
            true
        }
        R.id.normal_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 3000 // update a cada 1 segundo
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Stop receiving coordinates in onPause event
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("HELDER", "onPause - removeLocationUpdates")
        sensorManager.unregisterListener(this, brightness)
        sensorManager.unregisterListener(this, accelerometerSensor)
        sensorManager.unregisterListener(this, magnetometerSensor)
    }

    public override fun onResume() {
        super.onResume()
        startLocationUpdates()
        Log.d("HELDER", "onResumo - startLocationUpdates")
        sensorManager.registerListener(this, brightness, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun occurrenceFabClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked // se era true passa a false e vice versa :D
    }

    private fun setVisibility(clicked: Boolean) {
        if (clicked) {
            new_occurrenceFab.visibility = View.VISIBLE
            list_occurrenceFab.visibility = View.VISIBLE
        } else {
            new_occurrenceFab.visibility = View.INVISIBLE
            list_occurrenceFab.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (clicked) {
            new_occurrenceFab.startAnimation(fromBottom)
            list_occurrenceFab.startAnimation(fromBottom)
            _occurrenceFab.startAnimation(rotateOpen)
        } else {
            new_occurrenceFab.startAnimation(toBottom)
            list_occurrenceFab.startAnimation(toBottom)
            _occurrenceFab.startAnimation(rotateClose)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (clicked) {
            new_occurrenceFab.isClickable = false
            list_occurrenceFab.isClickable = false
        } else {
            new_occurrenceFab.isClickable = true
            list_occurrenceFab.isClickable = true
        }
    }

    private fun filterByType(type: String) {

        Toast.makeText(this, type, Toast.LENGTH_LONG).show()

        for (marker in markers) {
            marker.isVisible = markersTypeHashMap[marker] == type
        }
    }

    private fun filterByDistance(location: Location, markers: ArrayList<Marker>, range: Int) {
        for (marker in markers) {
            var distanceWidth = FloatArray(1)

            //calculate the distance between left <-> right of map on screen
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                marker.position.latitude,
                marker.position.longitude,
                distanceWidth
            )
            marker.isVisible = distanceWidth[0] < range
        }
    }

    private fun openDialogType() {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(R.string.filter_occorrences)


        builder.setItems(R.array.Filter_types) { _, which ->
            when (which) {
                0 -> {
                    filterByType("Traffic")
                }
                1 -> {
                    filterByType("Accident")
                }
                2 -> {
                    filterByType("Construction")
                }
                3 -> {
                    filterByType("Protest")
                }
                4 -> {
                    filterByType("Other")
                }
                5 -> {
                    showAllMarkers()
                }
            }
        }

        builder.setIcon(android.R.drawable.ic_menu_mapmode)

        //performing cancel action
        builder.setNeutralButton(R.string.cancel) { _, _ ->
            Toast.makeText(applicationContext, R.string.canceled, Toast.LENGTH_LONG).show()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showAllMarkers() {
        for (marker in markers) {
            marker.isVisible = true
        }
    }

    override fun onMapLongClick(position: LatLng?) {
        if (Build.VERSION.SDK_INT >= 29) {
            //Background Permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                handleMapLong(position)
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        FINE_LOCATION_ACCESS_REQUEST_CODE
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        FINE_LOCATION_ACCESS_REQUEST_CODE
                    )
                }
            }
        } else {
            handleMapLong(position)
        }
    }

    private fun handleMapLong(position: LatLng?) {
        addAreaGeofence(position!!, range.toDouble())
        addGeofence(position, range)
    }

    private fun addGeofence(latlng: LatLng, radius: Float) {
        val geofence = geoFenceHelper.getGeofence(
            GEOFENCE_ID,
            latlng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER
        )
        val geofencingRequest = geoFenceHelper.getGeofencingRequest(geofence)
        val pendingIntent = geoFenceHelper.getPendingIntent()
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener { }
            .addOnFailureListener { p0 -> val errorMessage = geoFenceHelper.getErrorString(p0) }
    }

    private fun addAreaGeofence(center: LatLng, radius: Double) {
        var areaOption: CircleOptions = CircleOptions()
        areaOption.center(center)
        areaOption.radius(radius)
        areaOption.strokeColor(Color.argb(255, 255, 0, 0))
        areaOption.fillColor(Color.argb(64, 255, 0, 0))
        areaOption.strokeWidth(4f)
        map!!.addCircle(areaOption)

    }
}
