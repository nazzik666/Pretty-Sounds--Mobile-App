package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout

class SettingPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_page)

        val layout = findViewById<ConstraintLayout>(R.id.mainLayoutSetting)
        setupBackground(layout)

        val arrowBack: ImageButton = findViewById(R.id.arrow_backButton)
        arrowBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
