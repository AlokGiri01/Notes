package com.shagworld.notes.listener

interface NetworkListener {
    fun onStarted()
    fun onSuccess(response: Any)
    fun onFailure(obj: Any)

    fun onFailureMessage(message: String)
    fun onPerformOperation(which: Int, value: Any)

    fun onHandleUserIdIsEmpty()

}