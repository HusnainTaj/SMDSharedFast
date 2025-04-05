package com.smd.l226786.sharedfast.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.smd.l226786.sharedfast.R
import com.smd.l226786.sharedfast.models.Folder

class FolderAdapter(private val context: Context, private val folders: List<Folder>) : BaseAdapter() {
    override fun getCount(): Int = folders.size

    override fun getItem(position: Int): Any = folders[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false)
        val folder = getItem(position) as Folder

        val thumbnailView = view.findViewById<ImageView>(R.id.folder_thumbnail)
        val titleView = view.findViewById<TextView>(R.id.folder_title)

        titleView.text = folder.name
        
        val thumbnail = Folder.getThumbnail(context, folder.name)
        if (thumbnail != null) {
            Glide.with(context).load(thumbnail).into(thumbnailView)
        } else {
            Glide.with(context).load(R.drawable.placeholder).into(thumbnailView)
        }

        return view
    }
}
