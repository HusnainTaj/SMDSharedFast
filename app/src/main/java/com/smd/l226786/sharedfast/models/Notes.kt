package com.smd.l226786.sharedfast.models

data class Notes(
    val id: Int,
    val title: String,
    val timestamp: Long,
    val image: String,
    val folderId: Int
)
{
    companion object {
        val notesList = mutableListOf<Notes>(
            Notes(1, "Note 1", System.currentTimeMillis(), "image1.png", 1),
            Notes(2, "Note 2", System.currentTimeMillis(), "image2.png", 1),
            Notes(3, "Note 3", System.currentTimeMillis(), "image3.png", 2),
            Notes(4, "Note 4", System.currentTimeMillis( ), "image4.png", 2),
            Notes(5, "Note 5", System.currentTimeMillis(), "image5.png", 3)
        )
    }
}
