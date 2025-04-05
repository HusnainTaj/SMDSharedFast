package com.smd.l226786.sharedfast

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.smd.l226786.sharedfast.models.Notes
import java.io.File

class NotesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: ArrayAdapter<Notes>
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

        notesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredNotes)
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
