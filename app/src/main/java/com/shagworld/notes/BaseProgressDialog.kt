package com.shagworld.notes

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment


class BaseProgressDialog : DialogFragment() {

    companion object {
        const val DIALOG_TAG = "BaseProgressDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.MainAlertDialog)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.progress_dialog, null)

            builder.setView(dialogView)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val size = resources.getDimensionPixelSize(R.dimen.size_80dp)
        dialog?.window?.setLayout(size, size)
    }
}