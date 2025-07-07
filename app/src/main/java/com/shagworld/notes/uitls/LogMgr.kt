package com.shagworld.notes.uitls

import android.util.Log

class LogMgr {
    private val isLogEnabled = true
    private val TAG = "App-->"

    fun i(msg: String) {
        if (isLogEnabled) {
            Log.i(TAG, msg)
        }
    }

    fun e(msg: String?) {
        Log.i(TAG.toString() + "Exception", msg!!)
    }

}