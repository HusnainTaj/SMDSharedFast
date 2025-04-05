package com.smd.l226786.sharedfast.models

import android.content.Context
import java.io.File

data class Folder(val name: String) {
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
    }
}
