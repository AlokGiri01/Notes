package com.shagworld.notes.uitls

import android.content.Context
import android.graphics.Color
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shagworld.notes.listener.CallbackListener
import kotlin.random.Random

object Utils {
    fun showAlertMessage(
        context: Context, title: String, message: String, posText: String, NegText: String,
        callbackListener: CallbackListener
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(posText) { _, _ ->
                callbackListener.onNegativeClick()
            }
            .setPositiveButton(NegText) { _, _ ->
                callbackListener.onPositiveClick("")
            }
            .show()
    }

    fun getRandomColorWithOpacity(): Int {
        // Generate a random color with random opacity
        val alpha = Random.nextInt(50, 256)  // Opacity between 75% and 100%
        val red = Random.nextInt(256)
        val green = Random.nextInt(256)
        val blue = Random.nextInt(256)

        return Color.argb(alpha, red, green, blue)
    }
}