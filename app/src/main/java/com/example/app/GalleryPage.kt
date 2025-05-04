package com.example.app

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Intent
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GalleryPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_page)
        val layout = findViewById<ConstraintLayout>(R.id.mainLayoutGallery)
        setupBackground(layout)//из BaseActivity

        val imageButton = findViewById<ImageView>(R.id.imageButton_gallery)
        imageButton.setOnClickListener {
            layout.setBackgroundResource(R.drawable.fondark)
        }

        val closeButton_gallery: ImageButton = findViewById(R.id.closeButton_gallery)
        closeButton_gallery.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
