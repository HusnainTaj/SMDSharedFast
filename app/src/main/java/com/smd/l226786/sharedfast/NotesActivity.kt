package com.smd.l226786.sharedfast

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.smd.l226786.sharedfast.models.Notes
import com.smd.l226786.sharedfast.adapters.NotesAdapter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class NotesActivity : AppCompatActivity() {

    private lateinit var notesAdapter: NotesAdapter
    private var folderName: String = ""
    private var allNotes = listOf<Notes>()
    private var filteredNotes = mutableListOf<Notes>()
    private lateinit var searchEditText: EditText

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
                    fileData = imageData,
                    mimeType = "image/jpeg",
                    extension = "jpg"
                )

                newNote?.let { 
                    refreshNotesList()
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
                fileData = imageData,
                mimeType = "image/jpeg",
                extension = "jpg"
            )

            newNote?.let { 
                refreshNotesList()
            }
        }
    }

    private val pickFilesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                inputStream?.copyTo(byteArrayOutputStream)
                val fileData = byteArrayOutputStream.toByteArray()
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"

                val newNote = Notes.createNote(
                    context = this,
                    folderName = folderName,
                    title = "File ${System.currentTimeMillis()}",
                    fileData = fileData,
                    mimeType = mimeType,
                    extension = extension
                )

                newNote?.let { 
                    refreshNotesList()
                }
            }
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

        folderName = intent.getStringExtra("FOLDER_NAME") ?: ""
        allNotes = Notes.getNotes(this, folderName)
        filteredNotes = allNotes.toMutableList()

        notesAdapter = NotesAdapter(this, filteredNotes)
        val listView: ListView = findViewById(R.id.notes_list_view)
        listView.adapter = notesAdapter

        searchEditText = findViewById(R.id.search_notes_edit_text)
        setupSearchEditText()

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

        val importFilesButton: Button = findViewById(R.id.import_files_button)
        importFilesButton.setOnClickListener {
            pickFilesLauncher.launch("*/*")
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedNote = filteredNotes[position]
            showNoteOptionsDialog(selectedNote)
            true
        }
    }
    
    private fun setupSearchEditText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
    }
    
    private fun filterNotes(query: String?) {
        notesAdapter.clear()
        
        if (query.isNullOrBlank()) {
            notesAdapter.addAll(allNotes)
        } else {
            val searchText = query.toLowerCase().trim()
            val filtered = allNotes.filter { note ->
                note.title.toLowerCase().contains(searchText)
            }
            notesAdapter.addAll(filtered)
        }
        
        notesAdapter.notifyDataSetChanged()
    }
    
    private fun refreshNotesList() {
        allNotes = Notes.getNotes(this, folderName)
        filterNotes(searchEditText.text.toString())
    }

    private fun showNoteOptionsDialog(note: Notes) {
        val options = arrayOf("Rename", "Delete")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Note Options")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showRenameNoteDialog(note)
                1 -> {
                    if (Notes.deleteNote(this, note)) {
                        allNotes = allNotes.filter { it.id != note.id }
                        filterNotes(searchEditText.text.toString())
                        Snackbar.make(findViewById(R.id.main), "Note '${note.title}' deleted", Snackbar.LENGTH_SHORT).show()
                    } else {
                        Snackbar.make(findViewById(R.id.main), "Failed to delete note '${note.title}'", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showRenameNoteDialog(note: Notes) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Note")

        val viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_folder, null, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_folder_name)
        input.setText(note.title.substringBeforeLast('.'))

        builder.setView(viewInflated)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            val newNoteTitle = input.text.toString()
            if (newNoteTitle.isNotEmpty() && newNoteTitle != note.title) {
                if (Notes.renameNote(this, note, newNoteTitle)) {
                    refreshNotesList()
                    Snackbar.make(findViewById(R.id.main), "Note renamed to '$newNoteTitle'", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(R.id.main), "Failed to rename note", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}

