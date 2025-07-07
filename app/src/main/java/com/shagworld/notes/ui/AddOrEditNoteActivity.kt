package com.shagworld.notes.ui

import android.os.Bundle
import android.text.TextUtils
import androidx.activity.OnBackPressedCallback
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.shagworld.notes.BaseActivity
import com.shagworld.notes.R
import com.shagworld.notes.databinding.ActivityAddOrEditNoteBinding
import com.shagworld.notes.databinding.ToolbarHeaderBinding
import com.shagworld.notes.listener.CallbackListener
import com.shagworld.notes.listener.OkClickListener
import com.shagworld.notes.model.Note
import com.shagworld.notes.uitls.Constants
import com.shagworld.notes.uitls.EncryptionHelper.decrypt
import com.shagworld.notes.uitls.EncryptionHelper.encrypt
import com.shagworld.notes.uitls.Pref
import com.shagworld.notes.uitls.Utils
import com.shagworld.notes.uitls.serializable
import com.shagworld.notes.uitls.toast

class AddOrEditNoteActivity : BaseActivity() {
    private lateinit var binding: ActivityAddOrEditNoteBinding
    private lateinit var toolbarBinding: ToolbarHeaderBinding
    var length = 0
    var note: Note? = null
    var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrEditNoteBinding.inflate(layoutInflater)
        toolbarBinding = binding.toolbar
        setContentView(binding.root)
        initializer()
        note = intent.serializable("ID")
        note?.let {
            binding.etTitle.setText(note?.title)
            binding.editTextNote.setText(decrypt(note?.noteDetails.toString()) )
        }
        length = binding.editTextNote.text.length
        toolbarBinding.tvAction.text = "Save"
        toolbarBinding.tvAction.setOnClickListener {
            saveNote()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.editTextNote.text.length != length) {
                    displayGoBackMessage()
                } else {
                    finish()
                }
            }
        })
    }

    private fun saveNote() {
        var noteDetails = binding.editTextNote.text.toString()
        if (TextUtils.isEmpty(noteDetails)) {
            toast("Nothing to Save")
        } else {
            val encryptedNote =  encrypt(noteDetails)
            val note = hashMapOf(
                "title" to binding.etTitle.text?.toString()?.trim(),
                "noteId" to 1,
                "noteDetails" to encryptedNote,
                "color" to 0,
                "size" to 0
            )
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (TextUtils.isEmpty(userId)) {
                toast("User not found,Please login again.")
            } else {
                db.collection(Constants.NOTES).document(userId!!)
                    .collection(Constants.USER_NOTES)
                    .add(note)
                    .addOnSuccessListener {
                        toast("Notes save successfully")
                    }
                    .addOnFailureListener {
                        toast("Some error occurred.${it.message}")
                    }
            }

            /*  val title = binding.etTitle.text?.toString()?.trim()
              val noteId = Pref.notes.size + 1
              val note = Note(noteId, title.toString(), noteDetails,0,0)
              val list = Pref.notes
              Pref.notes.clear()
              list.add(note)
              Pref.notes = list*/
        }
        finish()
    }

    private fun initializer() {
        initWithExitMessage(true, object : OkClickListener {
            override fun onclick(pos: Int, data: String) {
                if (binding.editTextNote.text.length != length) {
                    displayGoBackMessage()
                } else {
                    finish()
                }
            }
        })
        setScreenTitle(R.string.add_note)
    }

    private fun displayGoBackMessage() {
        Utils.showAlertMessage(this@AddOrEditNoteActivity,
            getString(R.string.app_name),
            getString(R.string.go_back_message),
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
}
