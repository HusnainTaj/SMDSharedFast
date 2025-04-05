package com.smd.l226786.sharedfast.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.smd.l226786.sharedfast.R
import com.smd.l226786.sharedfast.models.Notes

class NotesAdapter(context: Context, notes: List<Notes>) : ArrayAdapter<Notes>(context, 0, notes) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: View.inflate(context, R.layout.item_note, null)
        val note = getItem(position)

        val imageView = view.findViewById<ImageView>(R.id.note_image)
        val titleView = view.findViewById<TextView>(R.id.note_title)
        val timestampView = view.findViewById<TextView>(R.id.note_timestamp)

        note?.let {
            Glide.with(context).load(it.filePath).into(imageView)
            titleView.text = it.title
            timestampView.text = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", it.timestamp)
        }

        return view
    }
}
