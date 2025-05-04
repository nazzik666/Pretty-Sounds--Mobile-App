package com.example.app

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout

class SearchPage : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_page)

        val layout = findViewById<ConstraintLayout>(R.id.mainLayoutSearch)
        setupBackground(layout)
    }
}
