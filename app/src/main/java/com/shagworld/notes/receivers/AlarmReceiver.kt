package com.shagworld.notes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
//        var intent :Intent= Intent(this,AlarmActivity::class.java)
//        context.startActivity(intent)
        val notificationId = 1
        val notificationBuilder = NotificationCompat.Builder(context, "REMINDER_CHANNEL_ID")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder")
            .setContentText("It's time for your scheduled reminder!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        /*  with(NotificationManagerCompat.from(context)) {
              notify(notificationId, notificationBuilder.build())
          }*/
        Toast.makeText(context, "Alarm Snoozed for 5 minutes", Toast.LENGTH_LONG).show()
    }

    private fun openActivity() {

    }
}