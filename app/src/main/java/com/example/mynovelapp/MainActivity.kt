package com.example.mynovelapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val SPLASH_SCREEN: Int = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set full-screen flags
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_main)

        // Initialize views
        val novelname: TextView = findViewById(R.id.textView)
        val noveltype: TextView = findViewById(R.id.textView2)

        // Load bottom animation
        val bottomanim: Animation = AnimationUtils.loadAnimation(this, R.anim.bottomanim)

        // Apply animation to both TextViews
        novelname.startAnimation(bottomanim)
        noveltype.startAnimation(bottomanim)

        // Navigate to the next activity after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN.toLong())
    }
}
