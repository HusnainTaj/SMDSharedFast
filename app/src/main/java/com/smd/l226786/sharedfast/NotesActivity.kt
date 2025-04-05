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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class NotesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private var folderName: String = ""

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                inputStream?.copyTo(byteArrayOutputStream)
                val imageData = byteArrayOutputStream.toByteArray()

                val newNote = Notes.createNote(
                    context = this,
                    folderName = folderName,
                    title = "Note ${System.currentTimeMillis()}",
                    imageData = imageData
                )

                newNote?.let { 
                    val updatedNotes = Notes.getNotes(this, folderName)
                    notesAdapter.clear()
                    notesAdapter.addAll(updatedNotes)
                    notesAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            val inputStream: InputStream? = contentResolver.openInputStream(currentPhotoUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            inputStream?.copyTo(byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()

            val newNote = Notes.createNote(
                context = this,
                folderName = folderName,
                title = "Note ${System.currentTimeMillis()}",
                imageData = imageData
            )

            newNote?.let { 
                val updatedNotes = Notes.getNotes(this, folderName)
                notesAdapter.clear()
                notesAdapter.addAll(updatedNotes)
                notesAdapter.notifyDataSetChanged()
            }
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

        folderName = intent.getStringExtra("FOLDER_NAME") ?: ""
        val filteredNotes = Notes.getNotes(this, folderName)

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
