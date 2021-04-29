package com.example.helpcity.adapters

import android.content.Intent
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.helpcity.OccurrenceActivity
import com.example.helpcity.OccurrenceDescription
import com.example.helpcity.R
import com.example.helpcity.api.Occurrence
import kotlinx.android.synthetic.main.recyclerview_occurrence.view.*
import java.lang.Integer.parseInt

const val OCCURRENCE_ID = "id"

class OccurrenceAdapter(
    val list: List<Occurrence>,
    private val occurrenceInterface: OccurrenceActivity
) : RecyclerView.Adapter<OccurrenceViewHolder>() {

    private var noteInterface = occurrenceInterface

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OccurrenceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_occurrence, parent, false)
        return OccurrenceViewHolder(itemView, noteInterface)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OccurrenceViewHolder, position: Int) {
        return holder.bind(list[position])
    }

    fun getOccurrenceId(position: Int): String {
        return list[position].id
    }
}

//Creates references to list element views that make up the each recycler view element
class OccurrenceViewHolder(itemView: View, noteInterface: OccurrenceActivity) :
    RecyclerView.ViewHolder(itemView) {
    val type: TextView = itemView.occurrenceType
    val description = itemView.occurrenceDescription

    //val location = itemView.report_list_location
    val id = itemView.occurrenceId
    var occurrence: Occurrence? = null

    fun bind(occurrence: Occurrence) {
        this.occurrence = occurrence
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        type.text = occurrence.type
        description.text = occurrence.description
        id.text = occurrence.id
    }

    init {
        itemView.setOnClickListener { v: View ->
            val intent = Intent(v.context, OccurrenceDescription::class.java).apply {
                putExtra(OCCURRENCE_ID, parseInt(id.text.toString()))
            }

            Log.e("HELDER", id.text.toString())

            v.context.startActivity(intent)
        }
    }
}


