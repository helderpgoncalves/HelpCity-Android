package com.example.helpcity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.example.helpcity.api.EndPoints
import com.example.helpcity.api.ServerResponse
import com.example.helpcity.api.ServiceBuilder
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_new_occurrence.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*


open class NewOccurrenceActivity : AppCompatActivity() {

    // last know location
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // location periodic updates
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val REQUEST_CODE = 50
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val REQUEST_PICK_PHOTO = 2

    private var mediaPath: String? = null

    private var loc: LatLng? = null

    private var isImageDefault: Boolean = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_occurrence)

        //Toolbar
        setSupportActionBar(findViewById(R.id.new_occurrence_toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.new_occurrence)
        }

        // On Pick Image Button
        pickImage.setOnClickListener {
            uploadImage()
        }

        // Quando clicado butao de criar nova occurrencia
        new_occurrence_button.setOnClickListener {
            newOccurrence()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        // location periodic updates - função que vai tratar
        // de quando chega uma nova coordenada
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                loc = LatLng(lastLocation.latitude, lastLocation.longitude)
            }
        }
    }

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_CODE)
        } else {
            // TODO
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun newOccurrence() {

        val type = spinnerView.text.toString()
        val description = new_occurrence_description.text.toString()
        val userId = AppPreferences.id

        if (type.isEmpty() || description.isEmpty() || pickImage.text.equals(R.string.pick_image) || isImageDefault) {
            Toast.makeText(this, R.string.blankFields, Toast.LENGTH_LONG).show()
        } else {

            /*
            // IMAGEM
            val baos = ByteArrayOutputStream()
            val pic: Bitmap = (preview.drawable as BitmapDrawable).bitmap
            pic.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val finalImage: String = Base64.getEncoder().encodeToString(baos.toByteArray())

             */
            // bitmap da preview
            val bitmap = (preview.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            // blob is an binary large object
            val finalImage: String = Base64.getEncoder().encodeToString(stream.toByteArray())

            val request = ServiceBuilder.buildService(EndPoints::class.java)
            val call = request.newOccurrence(
                type,
                description,
                finalImage,
                loc!!.latitude.toString(),
                loc!!.longitude.toString(),
                userId.toInt()
            )

            call.enqueue(object : Callback<ServerResponse> {
                override fun onResponse(
                    call: Call<ServerResponse>,
                    response: Response<ServerResponse>
                ) {
                    if (response.isSuccessful) {
                        val insertResult = response.body()!!

                        if (insertResult.status) {
                            Toast.makeText(
                                this@NewOccurrenceActivity,
                                R.string.occurrence_create_success,
                                Toast.LENGTH_LONG
                            ).show()

                            val intent = Intent(this@NewOccurrenceActivity, MapActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@NewOccurrenceActivity,
                                R.string.occurrence_create_error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Toast.makeText(this@NewOccurrenceActivity, "${t.message}", Toast.LENGTH_LONG)
                        .show()
                }
            })
        }
    }


    // Este metodo gera um diaolog com 3 opções e retorna um resultado
    private fun uploadImage() {
        MaterialDialog.Builder(this)
            .title(R.string.uploadImages)
            .items(R.array.uploadImages)
            .itemsIds(R.array.itemIds)
            .itemsCallback { _, _, which, _ ->
                when (which) {
                    0 -> {
                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                        startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO)
                    }
                    1 -> captureImage()
                    2 -> {
                        pickImage.setText(R.string.pick_image)
                        isImageDefault = true
                        preview.setImageBitmap(null)
                        preview.setImageResource(R.drawable.ic_baseline_image_24)
                    }
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                val takenImage = data?.extras?.get("data") as Bitmap
                preview.setImageBitmap(takenImage)
                pickImage.setText(R.string.remove_or_change)
                isImageDefault = false
            } else if (requestCode == REQUEST_PICK_PHOTO) {
                // Get the Image from data
                val selectedImage = data?.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor =
                    contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                assert(cursor != null)
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                mediaPath = cursor.getString(columnIndex)
                // Set the Image in ImageView for Previewing the Media
                preview.setImageBitmap(BitmapFactory.decodeFile(mediaPath))
                isImageDefault = false
                cursor.close()

                pickImage.setText(R.string.remove_or_change)
            }
        } else if (requestCode != Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
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

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 2000 // update a cada 1 segundo
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}


