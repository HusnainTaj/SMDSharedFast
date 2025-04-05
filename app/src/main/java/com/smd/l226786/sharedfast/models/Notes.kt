package com.smd.l226786.sharedfast.models

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

data class Notes(
    val id: Int,
    var title: String,
    val timestamp: Long,
    var filePath: String, // can be an image or any other file
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

        fun createNote(
            context: Context,
            folderName: String,
            title: String,
            fileData: ByteArray,
            mimeType: String = "image/jpeg",
            extension: String = "jpg"
        ): Notes? {
            val folderUri = if (mimeType.startsWith("image")) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri("external")
            }
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

        fun renameNote(context: Context, note: Notes, newNoteTitle: String): Boolean {
            val oldNoteFile = File(note.filePath)
            val newNoteFile = File(oldNoteFile.parent, "$newNoteTitle.${oldNoteFile.extension}")
            return if (oldNoteFile.renameTo(newNoteFile)) {
                note.title = newNoteTitle
                note.filePath = newNoteFile.absolutePath
                true
            } else {
                false
            }
        }

        fun deleteNote(context: Context, note: Notes): Boolean {
            val noteFile = File(note.filePath)
            return noteFile.delete()
        }
    }
}
