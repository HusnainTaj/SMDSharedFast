package com.smd.l226786.sharedfast

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.smd.l226786.sharedfast.databinding.ActivityMainBinding
import com.smd.l226786.sharedfast.models.Folder

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var folderAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        folderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, Folder.getFolders(this).map { it.name })
        val listView: ListView = findViewById(R.id.folder_list_view)
        listView.adapter = folderAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFolder = Folder.getFolders(this)[position]
            val intent = Intent(this, NotesActivity::class.java).apply {
                putExtra("FOLDER_NAME", selectedFolder.name)
            }
            startActivity(intent)
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
                    folderAdapter.add(it.name)
                    folderAdapter.notifyDataSetChanged()
                    Snackbar.make(binding.root, "Folder '$folderName' created", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }

        builder.show()
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
}
