package com.shagworld.notes.uitls

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.shagworld.notes.listener.OkClickListener
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created By Alok G on 05-12-2023.
 */

/*
* Start Activity from Activity
* */
inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    if (requestCode == -1)
        startActivity(intent)
    else
        startActivityForResult(intent, requestCode)
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

fun Context.createImageFile(pltOrCrtValue: String, whichImage: Int): File {
    // Create an image file name
    val userName = Pref.userLoggedIn
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "${userName}_${pltOrCrtValue}_${whichImage}_" + timeStamp + "_"
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    /*  return File.createTempFile(
          imageFileName, *//* prefix *//*
        ".jpg", *//* suffix *//*
        externalCacheDir      *//* directory *//*
    )*/
    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        storageDir      /* directory */
    )
}

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}

fun View.showSnackBarWithAction(message: String, clickListener: OkClickListener) {
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE).also { snackBar ->
        snackBar.setAction("Ok") {
            snackBar.dismiss()
            clickListener.onclick(0, "")
        }
    }.show()
}

fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

fun EditText.autoCapitalize() {
    filters += InputFilter.AllCaps()
}

fun AlertDialog.displayProgressDialog() {
    if (!isShowing) {
        show()
    }
}

fun AlertDialog.hideProgressDialog() {
    dismiss()
}

