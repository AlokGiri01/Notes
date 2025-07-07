package com.shagworld.notes

import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.shagworld.notes.listener.OkClickListener


abstract class BaseActivity : AppCompatActivity() {

    private lateinit var tvVersion: TextView
    lateinit var toolbar: Toolbar
    var progressDialog = BaseProgressDialog()


    fun init(isDisplayBack: Boolean) {
        toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        if (isDisplayBack) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            toolbar.setNavigationOnClickListener { view: View? -> finish() }
        } else {
            supportActionBar?.setLogo(R.mipmap.ic_launcher)
            supportActionBar?.setDisplayUseLogoEnabled(true)
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvVersion = findViewById<TextView>(R.id.tvAction)
    }

    fun initWithExitMessage(isDisplayBack: Boolean, clickListener: OkClickListener) {
        toolbar = findViewById<Toolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        if (isDisplayBack) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            toolbar.setNavigationOnClickListener { clickListener.onclick(0, "") }
        } else {
            supportActionBar?.setLogo(R.mipmap.ic_launcher)
            supportActionBar?.setDisplayUseLogoEnabled(true)
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tvVersion = findViewById<TextView>(R.id.tvAction)
    }

    fun setScreenTitle(resId: Int) {
        toolbar.title = getString(resId)
    }

    fun setScreenTitle(title: String) {
        toolbar.title = title
    }

    fun setVersion(version: String) {
        tvVersion.text = version
    }
}