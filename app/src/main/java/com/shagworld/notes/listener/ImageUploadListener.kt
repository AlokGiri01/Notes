package com.shagworld.notes.listener

interface ImageUploadListener {
    fun onUploadSuccess(response: Any)

    fun onUploadFailure(message: String?)

}