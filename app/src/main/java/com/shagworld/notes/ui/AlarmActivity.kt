package com.shagworld.notes.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shagworld.notes.databinding.ActivityAlarmBinding
import com.shagworld.notes.receivers.AlarmReceiver
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {
    private lateinit var biding: ActivityAlarmBinding
    private lateinit var snoozeTime :Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        biding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(biding.root)
        biding.btnStop.setOnClickListener {
            snoozeTime = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 5)  // Snooze for 5 minutes
            }
        }

            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime.timeInMillis, pendingIntent)

            Toast.makeText(this, "Alarm Snoozed for 5 minutes", Toast.LENGTH_SHORT).show()
            finish()  // Close the AlarmActivity   }
        biding.btnSnooze.setOnClickListener {  finish()   }
    }
}