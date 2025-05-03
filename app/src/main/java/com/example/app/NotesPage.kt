package com.example.app

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout

class NotesPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_page)

        val layout = findViewById<ConstraintLayout>(R.id.mainLayoutNotes)
        setupBackground(layout)
    }
}
