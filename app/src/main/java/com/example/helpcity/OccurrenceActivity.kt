package com.example.helpcity

import OccurrenceAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helpcity.api.EndPoints
import com.example.helpcity.api.Occurrence
import com.example.helpcity.api.ServerResponse
import com.example.helpcity.api.ServiceBuilder
import kotlinx.android.synthetic.main.activity_occurrence.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OccurrenceActivity : AppCompatActivity() {

    lateinit var occurrences: List<Occurrence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_occurrence)

        setSupportActionBar(findViewById(R.id.occurrenceListToolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.my_occurrences)
        }

        getAllUserOccurrences()

        val swipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val adapter = OccurrenceAdapter(occurrences, this@OccurrenceActivity)
                val id = adapter.getOccurrenceId(pos)
                deleteOccurrenceById(id)
                adapter.notifyItemRemoved(pos)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerOccurrences)

    }

    private fun deleteOccurrenceById(id: String) {
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.deleteOccurrenceById(id)

        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                if (response.body()!!.status) {
                    Toast.makeText(
                        this@OccurrenceActivity,
                        R.string.occurrence_deleted,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@OccurrenceActivity,
                        response.body().toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Toast.makeText(this@OccurrenceActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getAllUserOccurrences() {
        // Call the service

        val userId = AppPreferences.id

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getUserOccurrences(userId)

        call.enqueue(object : Callback<List<Occurrence>> {
            override fun onResponse(
                call: Call<List<Occurrence>>,
                response: Response<List<Occurrence>>
            ) {
                if (response.isSuccessful) {
                    occurrences = response.body()!!
                    recyclerOccurrences.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@OccurrenceActivity)
                        adapter = OccurrenceAdapter(occurrences, this@OccurrenceActivity)
                    }
                }
            }
            override fun onFailure(call: Call<List<Occurrence>>, t: Throwable) {
                Toast.makeText(this@OccurrenceActivity, R.string.occurrences_not_found, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_delete_all -> {

                val builder = AlertDialog.Builder(this)
                builder.setPositiveButton(R.string.yes) { _, _ ->
                    deleteAllUserOccurrences()
                    Toast.makeText(this, R.string.all_notes_cleared, Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton(R.string.no) { _, _ -> }
                builder.setTitle(R.string.delete_everything)
                builder.setMessage(R.string.delete_everything_confirmation)
                builder.create().show()

                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteAllUserOccurrences() {
        val userId = AppPreferences.id

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.deleteAllUserOccurrences(userId)

        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(
                call: Call<ServerResponse>,
                response: Response<ServerResponse>
            ) {
                if (response.isSuccessful) {
                    // TODO
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Toast.makeText(this@OccurrenceActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        return true
    }

}