package com.smd.l226786.sharedfast

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.smd.l226786.sharedfast.models.Notes
import java.io.File

class NotesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private var folderId: Int = -1

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val newNote = Notes(
                    id = Notes.notesList.size + 1,
                    title = "New Note",
                    timestamp = System.currentTimeMillis(),
                    image = uri.toString(),
                    folderId = folderId
                )
                Notes.notesList.add(newNote)
            }
            // Refresh the adapter with the updated notes list
            val updatedNotes = Notes.notesList.filter { it.folderId == folderId }
            notesAdapter.clear()
            notesAdapter.addAll(updatedNotes)
            notesAdapter.notifyDataSetChanged()
        }
    }

    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            val newNote = Notes(
                id = Notes.notesList.size + 1,
                title = "New Note",
                timestamp = System.currentTimeMillis(),
                image = currentPhotoUri.toString(),
                folderId = folderId
            )
            Notes.notesList.add(newNote)
            // Refresh the adapter with the updated notes list
            val updatedNotes = Notes.notesList.filter { it.folderId == folderId }
            notesAdapter.clear()
            notesAdapter.addAll(updatedNotes)
            notesAdapter.notifyDataSetChanged()
        }
    }

    private lateinit var currentPhotoUri: Uri

    private inner class NotesAdapter(private val notes: List<Notes>) : ArrayAdapter<Notes>(this@NotesActivity, 0, notes) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_note, parent, false)
            val note = getItem(position)

            val imageView = view.findViewById<ImageView>(R.id.note_image)
            val titleView = view.findViewById<TextView>(R.id.note_title)
            val timestampView = view.findViewById<TextView>(R.id.note_timestamp)

            note?.let {
                Glide.with(this@NotesActivity).load(it.image).into(imageView)
                titleView.text = it.title
                timestampView.text = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", it.timestamp)
            }

            return view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        folderId = intent.getIntExtra("FOLDER_ID", -1)
        val filteredNotes = Notes.notesList.filter { it.folderId == folderId }

        notesAdapter = NotesAdapter(filteredNotes)
        val listView: ListView = findViewById(R.id.notes_list_view)
        listView.adapter = notesAdapter

        val importImagesButton: Button = findViewById(R.id.import_images_button)
        importImagesButton.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        val captureImageButton: Button = findViewById(R.id.capture_image_button)
        captureImageButton.setOnClickListener {
            val photoFile = File.createTempFile("IMG_", ".jpg", externalCacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            currentPhotoUri = FileProvider.getUriForFile(this, "com.smd.l226786.sharedfast.provider", photoFile)
            captureImageLauncher.launch(currentPhotoUri)
        }
    }
}
