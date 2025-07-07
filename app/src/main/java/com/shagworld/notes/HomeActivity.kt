package com.shagworld.notes

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.shagworld.notes.adapter.NoteAdapter
import com.shagworld.notes.databinding.ActivityHomeBinding
import com.shagworld.notes.databinding.DialogReminderBinding
import com.shagworld.notes.listener.CallbackListener
import com.shagworld.notes.model.Note
import com.shagworld.notes.receivers.AlarmReceiver
import com.shagworld.notes.ui.AddOrEditNoteActivity
import com.shagworld.notes.uitls.Constants
import com.shagworld.notes.uitls.EncryptionHelper.decryptAESKeyWithPassword
import com.shagworld.notes.uitls.EncryptionHelper.encryptAESKeyWithPassword
import com.shagworld.notes.uitls.EncryptionHelper.generateAESKey
import com.shagworld.notes.uitls.EncryptionHelper.secretKeyToBase64
import com.shagworld.notes.uitls.EncryptionHelper.uploadEncryptedAESKeyToFirestore
import com.shagworld.notes.uitls.LogMgr
import com.shagworld.notes.uitls.Pref
import com.shagworld.notes.uitls.Utils
import com.shagworld.notes.uitls.hide
import com.shagworld.notes.uitls.launchActivity
import com.shagworld.notes.uitls.show
import com.shagworld.notes.uitls.toast
import java.util.Calendar

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var noteAdapter: NoteAdapter? = null
    var noteList = ArrayList<Note>()
    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var currentDay: Int = 0
    private lateinit var bindingReminder: DialogReminderBinding
    var selectedHour = 0
    var selectedMinute = 0
    var selectedDate = 0
    var selectedMonth = 0
    var selectedYear = 0
    var name = ""

    var firestore = Firebase.firestore
    lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init(false)
        name = intent.getStringExtra("NAME").toString()
        binding.tvUSer.text = "Hello $name"
        setScreenTitle(R.string.app_name)
        userId = FirebaseAuth.getInstance().currentUser!!.uid

        if (TextUtils.isEmpty(Pref.secretAESKey)) {
            LogMgr().i("Pref.AESKey is empty")
            Firebase.firestore.collection(Constants.SECURE_KEY).document(userId)
                .get()
                .addOnSuccessListener { result ->
                    if (result.exists()) {
                        LogMgr().i("result.exists()")
                        val encryptedMap = result.data as Map<String, String>
                        val encryptedKey = encryptedMap["encryptedKey"] ?: ""
                        val salt = encryptedMap["salt"] ?: ""
                        val iv = encryptedMap["iv"] ?: ""
                        val password = "abc123"
                        val decryptedAESKey =
                            decryptAESKeyWithPassword(encryptedKey, salt, iv, password)

                        Pref.secretAESKey =  secretKeyToBase64(decryptedAESKey)
                        getAllNotes()

                    } else {
                        LogMgr().i("result does not exists()")
                        val password = "abc123"

                        val aesKey = generateAESKey()
                        LogMgr().i("aesKey-->$aesKey")
                        val map = encryptAESKeyWithPassword(aesKey, password)
                        LogMgr().i("map-->$map")
//                        LogMgr().i("encryptedKey-->$encryptedKey")
                        uploadEncryptedAESKeyToFirestore(aesKey,map, userId)
                        getAllNotes()
                    }
                }
                .addOnFailureListener {
                    toast("Some error occurred while getting KEY")
                    finish()
                }
        } else {
            LogMgr().i("Pref.AESKey is not empty")
            getAllNotes()
        }


        binding.layoutMessage.setOnClickListener { launchActivity<AddOrEditNoteActivity> { } }
        binding.fabAddNote.setOnClickListener { launchActivity<AddOrEditNoteActivity> { } }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (noteList.isNotEmpty()) {
                    if (s.isNotEmpty()) {
                        var list = ArrayList<Note>()
                        for (item in noteList) {
                            if (item.title != null && item.title.lowercase()
                                    .contains(s.toString().lowercase())
                            ) {
                                list.add(item)
                            } else if (item.noteDetails != null && item.noteDetails.contains(s.toString())) {
                                list.add(item)
                            }
                        }
                        setNoteRecyclerView(list)
                    } else {
                        setNoteRecyclerView(noteList)
                    }
                }
            }
        })
    }

    private fun setNoteRecyclerView(list: ArrayList<Note>) {
        noteAdapter = NoteAdapter(list)
        binding.rvNotes.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvNotes.adapter = noteAdapter
        noteAdapter?.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(item: Any) {
                var note = item as Note
                launchActivity<AddOrEditNoteActivity> {
                    putExtra("ID", note)
                }
            }

            override fun onLongClick(noteId: Int) {
                openOptionSelectionDialog(noteId)
            }
        })
    }

    private fun openOptionSelectionDialog(noteId: Int) {
        val options = arrayOf("Delete", "Add Reminder")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setCancelable(false)
        builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                0 -> {
                    displayDeleteConfirmationDialog(noteId)
                }

                1 -> {
                    showReminderDialog(this)
                }

            }
        })
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // Close the dialog
        }
        builder.show()
    }

    private fun displayDeleteConfirmationDialog(noteId: Int) {
        Utils.showAlertMessage(this,
            getString(R.string.app_name),
            getString(R.string.delete_message),
            getString(R.string.no),
            getString(R.string.yes),
            object : CallbackListener {
                override fun onPositiveClick(any: Any) {
                    finish()
                }

                override fun onNegativeClick() {

                }
            })
    }

    override fun onResume() {
        super.onResume()

    }

    fun getAllNotes() {
        noteList.clear()
        firestore.collection(Constants.NOTES)
            .document(userId)
            .collection(Constants.USER_NOTES)
            .get()
            .addOnSuccessListener { result ->
                for (note in result) {
                    val user = note.toObject(Note::class.java)
                    noteList.add(user)
                }
                if (noteList.isEmpty()) {
                    binding.rvNotes.hide()
                    binding.layoutMessage.show()
                    binding.fabAddNote.hide()
                } else {
                    binding.rvNotes.show()
                    binding.fabAddNote.show()
                    binding.layoutMessage.hide()
                }
                setNoteRecyclerView(noteList)
            }
            .addOnFailureListener { exception ->
                toast("Some error occurred. ${exception.message}")
            }
    }

    private fun showReminderDialog(context: Context) {
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH)
        currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        bindingReminder = DialogReminderBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(bindingReminder.root)
            .create()

        bindingReminder.tvTitle.text = "Add reminder"
        bindingReminder.tvDate.setOnClickListener { openDatePicker() }
        bindingReminder.tvTime.setOnClickListener {
            showTimePickerDialog { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                bindingReminder.tvTime.text = String.format("%02d:%02d", hour, minute)
            }
        }
        bindingReminder.btnSave.setOnClickListener {
            setReminder()
            dialog.dismiss()
        }
        bindingReminder.btnCancel.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun setReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, selectedDate)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.YEAR, selectedYear)
            set(Calendar.HOUR_OF_DAY, selectedHour)  // Example hour: 14:00 (2 PM)
            set(Calendar.MINUTE, selectedMinute)       // Example minute: 30
            set(Calendar.SECOND, 0)
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun openDatePicker() {
        val minDate = Calendar.getInstance().timeInMillis
        /*
                val maxDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 30)
                }.timeInMillis*/

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate = dayOfMonth
            selectedMonth = month
            selectedYear = year
            bindingReminder.tvDate.text = "$year/${month + 1}/$dayOfMonth"
        }, currentYear, currentMonth, currentDay)

        datePickerDialog.datePicker.minDate = minDate
//        datePickerDialog.datePicker.maxDate = maxDate
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        }, currentHour, currentMinute, true) // true for 24-hour format, false for 12-hour format
        timePickerDialog.show()
    }
}