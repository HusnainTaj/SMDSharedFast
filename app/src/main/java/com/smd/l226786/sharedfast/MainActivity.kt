package com.smd.l226786.sharedfast

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.smd.l226786.sharedfast.databinding.ActivityMainBinding
import com.smd.l226786.sharedfast.models.Folder
import java.io.File
import androidx.core.content.FileProvider

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var folderAdapter: FolderAdapter
    private var folders = mutableListOf<Folder>()

    private inner class FolderAdapter : BaseAdapter() {
        override fun getCount(): Int = folders.size

        override fun getItem(position: Int): Any = folders[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_folder, parent, false)
            val folder = getItem(position) as Folder

            val thumbnailView = view.findViewById<ImageView>(R.id.folder_thumbnail)
            val titleView = view.findViewById<TextView>(R.id.folder_title)

            titleView.text = folder.name
            
            val thumbnail = Folder.getThumbnail(this@MainActivity, folder.name)
            if (thumbnail != null) {
                Glide.with(this@MainActivity).load(thumbnail).into(thumbnailView)
            } else {
                Glide.with(this@MainActivity).load(R.drawable.placeholder).into(thumbnailView)
            }

            return view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        folders = Folder.getFolders(this).toMutableList()
        folderAdapter = FolderAdapter()
        val gridView: GridView = findViewById(R.id.folder_grid_view)
        gridView.adapter = folderAdapter

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedFolder = folders[position]
            val intent = Intent(this, NotesActivity::class.java).apply {
                putExtra("FOLDER_NAME", selectedFolder.name)
            }
            startActivity(intent)
        }

        gridView.setOnItemLongClickListener { _, _, position, _ ->
            val selectedFolder = folders[position]
            showFolderOptionsDialog(selectedFolder)
            true
        }

        binding.fab.setOnClickListener { view ->
            showCreateFolderDialog()
        }
    }

    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create New Folder")

        val viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_folder, null, false)
        val input = viewInflated.findViewById<EditText>(R.id.input_folder_name)

        builder.setView(viewInflated)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            val folderName = input.text.toString()
            if (folderName.isNotEmpty()) {
                val newFolder = Folder.createFolder(this, folderName)
                newFolder?.let {
                    folders.add(it)
                    folderAdapter.notifyDataSetChanged()
                    Snackbar.make(binding.root, "Folder '$folderName' created", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showFolderOptionsDialog(folder: Folder) {
        val options = arrayOf("Share", "Delete")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Folder Options")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> shareFolder(folder)
                1 -> deleteFolder(folder)
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun shareFolder(folder: Folder) {
        val folderPath = File(getExternalFilesDir(null), folder.name)
        val imageUris = folderPath.listFiles { file -> file.extension == "jpg" }?.map { file ->
            FileProvider.getUriForFile(this, "com.smd.l226786.sharedfast.provider", file)
        } ?: emptyList()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Images"))
    }

    private fun deleteFolder(folder: Folder) {
        val folderPath = File(getExternalFilesDir(null), folder.name)
        if (folderPath.deleteRecursively()) {
            folders.remove(folder)
            folderAdapter.notifyDataSetChanged()
            Snackbar.make(binding.root, "Folder '${folder.name}' deleted", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Failed to delete folder '${folder.name}'", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh folder list to get updated thumbnails
        folders = Folder.getFolders(this).toMutableList()
        folderAdapter.notifyDataSetChanged()
    }
}
