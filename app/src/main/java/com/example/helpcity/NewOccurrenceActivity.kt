package com.example.helpcity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_new_occurrence.*


open class NewOccurrenceActivity : AppCompatActivity() {


    private var fileUri: Uri? = null

    private var mediaPath: String? = null

    private var mImageFileLocation = ""
    private lateinit var pDialog: ProgressDialog
    private var postPath: String? = null


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

        initDialog()
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
                        preview.setImageResource(R.drawable.ic_baseline_image_24)
                    }
                }
            }
            .show()
    }

    private fun initDialog() {
        pDialog = ProgressDialog(this)
        pDialog.setMessage(getString(R.string.msg_loading))
        pDialog.setCancelable(true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    // Get the Image from data
                    val selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                    val cursor =
                        contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                    assert(cursor != null)
                    cursor!!.moveToFirst()

                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                    mediaPath = cursor.getString(columnIndex)

                    // Set the Image in ImageView for Previewing the Media
                    preview.setImageBitmap(BitmapFactory.decodeFile(mediaPath))
                    cursor.close()

                    preview.setPadding(0)
                    pickImage.setText(R.string.remove_or_change)


                    postPath = mediaPath
                    Log.d("HELDER", postPath.toString())
                }
            } else if (requestCode == CAMERA_PIC_REQUEST) {
                if (Build.VERSION.SDK_INT > 21) {

                    Glide.with(this).load(mImageFileLocation).into(preview)
                    postPath = mImageFileLocation

                } else {
                    Glide.with(this).load(fileUri).into(preview)
                    postPath = fileUri!!.path
                }

            }

        } else if (resultCode != Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show()
        }
    }


    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri")
    }

    // ATRAVÉS DA CAMERA DO TELEMOVEL
    private fun captureImage() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, CAMERA_PIC_REQUEST)

                /*
                TODO
                 */
            }
        }
    }

    companion object {
        private val REQUEST_TAKE_PHOTO = 0
        private val REQUEST_PICK_PHOTO = 2
        private val CAMERA_PIC_REQUEST = 1111

        private val TAG = NewOccurrenceActivity::class.java.simpleName
    }

}

