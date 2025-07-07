package com.shagworld.notes.model

import java.io.Serializable

data class Note(
    var noteId: Int,
    var title: String,
    var noteDetails: String,
    var color: Int,
    var size: Int
) : Serializable {
    constructor() : this(0, "", "", 0, 0)
}