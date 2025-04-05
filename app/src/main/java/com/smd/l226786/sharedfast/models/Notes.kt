package com.smd.l226786.sharedfast.models

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

data class Notes(
    val id: Int,
    val title: String,
    val timestamp: Long,
    val image: String,
    val folderName: String
) {
    companion object {
        fun getNotes(context: Context, folderName: String): List<Notes> {
            val notes = mutableListOf<Notes>()
            val folderPath = context.getExternalFilesDir(null)?.absolutePath + "/$folderName"
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        val id = file.hashCode()
                        val title = file.name
                        val timestamp = file.lastModified()
                        notes.add(Notes(id, title, timestamp, file.absolutePath, folderName))
                    }
                }
            }
            return notes
        }

        fun createNote(context: Context, folderName: String, title: String, imageData: ByteArray): Notes? {
            val folderUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val folderPath = context.getExternalFilesDir(null)?.absolutePath + "/$folderName"
            val folder = File(folderPath)
            if (!folder.exists()) folder.mkdirs()

            val imageFile = File(folder, "$title.jpg")
            FileOutputStream(imageFile).use { it.write(imageData) }

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, title)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, folderPath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri: Uri? = context.contentResolver.insert(folderUri, values)
            uri?.let {
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    outputStream?.write(imageData)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)

                val id = imageFile.hashCode()
                return Notes(id, title, System.currentTimeMillis(), imageFile.absolutePath, folderName)
            }
            return null
        }

        fun createNoteWithFile(context: Context, folderName: String, title: String, fileData: ByteArray, mimeType: String, extension: String): Notes? {
            val folderUri = MediaStore.Files.getContentUri("external")
            val folderPath = context.getExternalFilesDir(null)?.absolutePath + "/$folderName"
            val folder = File(folderPath)
            if (!folder.exists()) folder.mkdirs()

            val file = File(folder, "$title.$extension")
            FileOutputStream(file).use { it.write(fileData) }

            val values = ContentValues().apply {
                put(MediaStore.Files.FileColumns.DISPLAY_NAME, title)
                put(MediaStore.Files.FileColumns.MIME_TYPE, mimeType)
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, folderPath)
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }

            val uri: Uri? = context.contentResolver.insert(folderUri, values)
            uri?.let {
                context.contentResolver.openOutputStream(it).use { outputStream ->
                    outputStream?.write(fileData)
                }
                values.clear()
                values.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)

                val id = file.hashCode()
                return Notes(id, title, System.currentTimeMillis(), file.absolutePath, folderName)
            }
            return null
        }
    }
}
