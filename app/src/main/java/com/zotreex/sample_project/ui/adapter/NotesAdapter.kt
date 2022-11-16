package com.zotreex.sample_project.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.zotreex.sample_project.R
import com.zotreex.sample_project.domain.data.models.GeoNote

class NotesAdapter(private val dataSet: Array<GeoNote>, private val onClickAction: (String) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val wrapper: ConstraintLayout
        val address: TextView
        val note: TextView

        init {

            // Define click listener for the ViewHolder's View.
            address = view.findViewById(R.id.list_item_address)
            note = view.findViewById(R.id.list_item_note)
            wrapper = view.findViewById(R.id.notes_recycler_view_item)

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): NoteViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_view_item, viewGroup, false)

        return NoteViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: NoteViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.note.text = dataSet[position].note
        viewHolder.address.text = dataSet[position].address
        viewHolder.wrapper.setOnClickListener{
            onClickAction(dataSet[position].address)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}