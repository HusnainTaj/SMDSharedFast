package com.smd.l226786.sharedfast.models

import android.content.Context
import java.io.File

data class Folder(var name: String) {
    companion object {
        fun getFolders(context: Context): List<Folder> {
            val folders = mutableListOf<Folder>()
            val rootDir = context.getExternalFilesDir(null)
            rootDir?.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    folders.add(Folder(file.name))
                }
            }
            return folders
        }

        fun createFolder(context: Context, folderName: String): Folder? {
            val newFolder = File(context.getExternalFilesDir(null)?.absolutePath + "/$folderName")
            return if (newFolder.mkdirs()) {
                Folder(folderName)
            } else {
                null
            }
        }

        fun getThumbnail(context: Context, folderName: String): String? {
            val notes = Notes.getNotes(context, folderName)
            return if (notes.isNotEmpty()) notes[0].filePath else null
        }

        fun renameFolder(context: Context, folder: Folder, newFolderName: String): Boolean {
            val oldFolderPath = File(context.getExternalFilesDir(null), folder.name)
            val newFolderPath = File(context.getExternalFilesDir(null), newFolderName)
            return if (oldFolderPath.renameTo(newFolderPath)) {
                folder.name = newFolderName
                true
            } else {
                false
            }
        }

        fun deleteFolder(context: Context, folder: Folder): Boolean {
            val folderPath = File(context.getExternalFilesDir(null), folder.name)
            return folderPath.deleteRecursively()
        }
    }
}
