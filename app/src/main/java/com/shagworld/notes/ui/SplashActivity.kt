package com.shagworld.notes.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.shagworld.notes.HomeActivity
import com.shagworld.notes.R
import com.shagworld.notes.databinding.ActivitySplashBinding
import com.shagworld.notes.uitls.LogMgr
import com.shagworld.notes.uitls.Pref
import com.shagworld.notes.uitls.launchActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val animZoomIn: Animation =
            AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        binding.tv.startAnimation(animZoomIn)

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            LogMgr().i("currentUser-->$currentUser")
            LogMgr().i("currentUser Id-->${currentUser?.uid}")
            if (currentUser !=null) {
                launchActivity<HomeActivity> {
                    putExtra("NAME",currentUser.displayName)
                }
            } else {
                launchActivity<LoginActivity> { }
            }
            finish()
        }, 3000)
    }
}