package com.example.helpcity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.helpcity.api.EndPoints
import com.example.helpcity.api.Occurrence
import com.example.helpcity.api.ServiceBuilder
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
import kotlin.math.roundToInt


//This class allows you to interact with the map by adding markers, styling its appearance and
// displaying the user's location.
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val TAG = MapActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var occurrences: List<Occurrence>
    private lateinit var markers: ArrayList<Marker>

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

        // location periodic updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val loc = LatLng(lastLocation.latitude, lastLocation.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.3f)) // Follow me option
                // TODO
            }
        }

        continuousSlider.addOnChangeListener(Slider.OnChangeListener { slider, _, _ ->
          /*  Log.d(
                "addOnChangeListener",
                slider.value.toString()
            )

           */

            filterByDistance(lastLocation, markers, slider.value.roundToInt())

        })
        continuousSlider.setLabelFormatter { value: Float ->
            val nomeAntesDoSlider = resources.getString(R.string.range_of)
            val nomeMetros = resources.getString(R.string.meters)
            return@setLabelFormatter nomeAntesDoSlider.plus(" ${value.roundToInt()} ")
                .plus(nomeMetros)
        }

        // update location
        createLocationRequest()


        getAllOccurrences()

        // Ir para a Atividade de Criar novas Ocorrencias
        _occurrenceFab.setOnClickListener {
            occurrenceFabClicked()
        }

        new_occurrenceFab.setOnClickListener {
            val i = Intent(this, NewOccurrenceActivity::class.java)
            startActivity(i)
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

                if (response.isSuccessful) {

                    occurrences = response.body()!!

                    for (occurrence in occurrences) {

                        /*
                        TODO
                        METER OS MARKERS COM IMAGEM

                        // Creates Bitmap objects from various sources, including files, streams, and byte-arrays.
                        // https://developer.android.com/reference/kotlin/android/graphics/BitmapFactory

                        var imagemWeb =
                            "https://helpcity.000webhostapp.com/uploads/" + occurrence.image
                        var input: InputStream = URL(imagemWeb).openStream()
                        var imagemFinalBitmap = BitmapFactory.decodeStream(input)
                         */

                        var position = LatLng(occurrence.lat.toDouble(), occurrence.lng.toDouble())

                        val marker: Marker = map.addMarker(
                            MarkerOptions().position(position).title(occurrence.type)
                                .snippet(occurrence.description)
                        )

                        markers.add(marker) // Adiciona-mos ao nosso ArrayList para mais tarde conseguirmos mexer nela e fazer o que pretendemos
                        markersTypeHashMap[marker] = occurrence.type
                    }
                }
            }

            override fun onFailure(call: Call<List<Occurrence>>, t: Throwable) {
                Toast.makeText(this@MapActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        enableMyLocation()
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
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
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 1000 // update a cada 1 segundo
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // Stop receiving coordinates in onPause event
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("HELDER", "onPause - removeLocationUpdates")
    }

    public override fun onResume() {
        super.onResume()
        startLocationUpdates()
        Log.d("HELDER", "onResumo - startLocationUpdates")

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
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
            list_occurrenceFab.isClickable = false
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
}
