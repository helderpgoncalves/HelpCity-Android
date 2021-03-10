package com.example.helpcity.adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.helpcity.EditNoteActivity
import com.example.helpcity.R
import com.example.helpcity.entities.Note
import kotlin.coroutines.coroutineContext

class NoteListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = emptyList<Note>()

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteIdView: TextView = itemView.findViewById(R.id.noteId)
        val noteTitleView: TextView = itemView.findViewById(R.id.noteTitle)
        val noteDescriptionView: TextView = itemView.findViewById(R.id.noteDescription)

        // Ir para a Nota Selecionada
        init {
            itemView.setOnClickListener { v: View ->
                val i = Intent(v.context, EditNoteActivity::class.java)
                i.putExtra("title", noteTitleView.text)
                i.putExtra("description", noteDescriptionView.text)
                i.putExtra("id", adapterPosition.toString())
                v.context.startActivity(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_note, parent, false)
        return NoteListAdapter.NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = notes[position]

        holder.noteIdView.text = current.noteId.toString()
        holder.noteTitleView.text = current.noteTitle
        holder.noteDescriptionView.text = current.noteDescription
    }

    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    internal fun getNotes() : List<Note> {
        return this.notes
    }

    override fun getItemCount() = notes.size

    companion object {
        const val EXTRA_DETAILS = "com.example.helpcity.REPLY"
        const val noteId = "noteId"
        const val noteTitle = "noteTitle"
        const val noteDescription = "noteDescription"
    }
}
