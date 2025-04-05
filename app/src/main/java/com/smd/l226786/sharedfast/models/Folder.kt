package com.smd.l226786.sharedfast.models

data class Folder(val id: Int, val name: String) {
    companion object {
        val folderList = mutableListOf<Folder>(
            Folder(1, "Folder 1"),
            Folder(2, "Folder 2"),
            Folder(3, "Folder 3"),
            Folder(4, "Folder 4"),
            Folder(5, "Folder 5")
        )
    }
}
