package com.example.app

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

open class BaseActivity : AppCompatActivity() {

    protected lateinit var mainLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Этот метод можно оставить пустым или вызывать из наследников
    }

    @Suppress("MissingInflatedId")
    protected fun setupBackground(layout: ConstraintLayout) {
        this.mainLayout = layout

        // Учет системных панелей
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Установка фона
        val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        mainLayout.setBackgroundResource(
            if (isNight) R.drawable.fondark else R.drawable.fonday
        )

        // Поиск и обработка кнопки смены темы (если она есть в разметке)
        val themeButton = findViewById<ImageButton?>(R.id.themeButton6)
        themeButton?.setImageResource(if (isNight) R.drawable.day_night else R.drawable.night)
        themeButton?.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(
                if (isNight) AppCompatDelegate.MODE_NIGHT_NO
                else AppCompatDelegate.MODE_NIGHT_YES
            )
            recreate() // обновляем Activity
        }
    }
}
