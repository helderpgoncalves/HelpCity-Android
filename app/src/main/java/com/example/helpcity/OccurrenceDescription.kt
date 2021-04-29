package com.example.helpcity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.example.helpcity.api.EndPoints
import com.example.helpcity.api.Occurrence
import com.example.helpcity.api.ServerResponse
import com.example.helpcity.api.ServiceBuilder
import kotlinx.android.synthetic.main.activity_new_occurrence.*
import kotlinx.android.synthetic.main.activity_occurrence_description.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.util.*

const val OCCURRENCE_ID = "id"

class OccurrenceDescription : AppCompatActivity() {

    private var occurrenceId: Int = 0
    private val REQUEST_CODE = 50
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val REQUEST_PICK_PHOTO = 2
    private var mediaPath: String? = null
    private var isImageDefault: Boolean = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_occurrence_description)

        setSupportActionBar(findViewById(R.id.occurrenceDescriptionToolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.occurrence_description)
        }

        occurrenceId = intent.getIntExtra(OCCURRENCE_ID, 0)

        getOccurrence(occurrenceId)

        // On Pick Image Button
        pickImage_button.setOnClickListener {
            updateImage()
        }

        // Quando clicado butao de criar nova occurrencia
        _update_occurrence_button.setOnClickListener {
            updateOccurrence(occurrenceId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOccurrence(occurrenceId: Int) {

        val type = _occurrence_type.text.toString()
        val description = _occurrence_description.text.toString()
        val userId = AppPreferences.id.toInt()
        val id = occurrenceId.toString()
        val lat = latitude.text.toString()
        val lng = longitude.text.toString()

        if (_occurrence_type.text.isEmpty() || _occurrence_description.text.isEmpty()) {
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
            val bitmap = (_occurrence_image.drawable as BitmapDrawable).bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

            // blob is an binary large object
            val finalImage: String = Base64.getEncoder().encodeToString(stream.toByteArray())

            val request = ServiceBuilder.buildService(EndPoints::class.java)
            val call = request.updateOccurrence(
                id,
                type,
                description,
                finalImage
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
                                this@OccurrenceDescription,
                                R.string.occurrence_updated_success,
                                Toast.LENGTH_LONG
                            ).show()

                            val intent =
                                Intent(this@OccurrenceDescription, OccurrenceActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@OccurrenceDescription,
                                R.string.occurrence_updated_error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                    Toast.makeText(this@OccurrenceDescription, "${t.message}", Toast.LENGTH_LONG)
                        .show()
                }
            })
        }
    }

    private fun updateImage() {
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
                }
            }
            .show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                val takenImage = data?.extras?.get("data") as Bitmap
                _occurrence_image.setImageBitmap(takenImage)
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
                _occurrence_image.setImageBitmap(BitmapFactory.decodeFile(mediaPath))
                isImageDefault = false
                cursor.close()

            }
        } else if (requestCode != Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getOccurrence(id: Int) {

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getOccurrenceById(id)

        call.enqueue(object : Callback<Occurrence> {
            override fun onResponse(call: Call<Occurrence>, response: Response<Occurrence>) {
                if (response.isSuccessful) {

                    val urlImage =
                        "http://helpcity.000webhostapp.com/uploads/" + response.body()!!.image
                    var input: InputStream = URL(urlImage).openStream()
                    var bitmap = BitmapFactory.decodeStream(input)

                    _occurrence_image.setImageBitmap(bitmap)
                    _occurrence_type.text = response.body()!!.type
                    _occurrence_description.text = response.body()!!.description
                    latitude.text = "Latitude: " + response.body()!!.lat
                    longitude.text = "Longitude: " + response.body()!!.lat
                }
            }

            override fun onFailure(call: Call<Occurrence>, t: Throwable) {
                getOccurrence(id)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_occurrence_description, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_delete_occurrence -> {
                deleteThisOccurrence()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteThisOccurrence() {
        val id = this.occurrenceId

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.deleteOccurrenceById(id.toString())

        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                if (response.body()!!.status) {
                    Toast.makeText(
                        this@OccurrenceDescription,
                        R.string.occurrence_deleted,
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(this@OccurrenceDescription, OccurrenceActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@OccurrenceDescription,
                        response.body()!!.status.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Toast.makeText(this@OccurrenceDescription, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }
}