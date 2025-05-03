package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log // Для логування
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager // Для RecyclerView
import androidx.recyclerview.widget.RecyclerView // Для RecyclerView
import com.example.app.data.AppDatabase
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist
import com.example.app.ui.playlists.PlaylistAdapter // Адаптер для списку плейлистів
import com.example.app.ui.playlists.PlaylistsViewModel
import com.example.app.ui.playlists.ViewModelFactory
import com.google.android.material.textfield.TextInputLayout


class MainActivity : AppCompatActivity() {

    // ViewModel для роботи з плейлистами
    private val playlistsViewModel: PlaylistsViewModel by viewModels {
        val repository = PlaylistRepository(AppDatabase.getDatabase(this).playlistDao())
        ViewModelFactory(repository)
    }

    // UI елементи
    private lateinit var themeToggleButton1: ImageButton
    private lateinit var themeToggleButton2: ImageButton
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var playlistAdapter: PlaylistAdapter // Адаптер для RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ініціалізація основних UI елементів
        mainLayout = findViewById(R.id.mainLayout)
        themeToggleButton1 = findViewById(R.id.themeButton)
        themeToggleButton2 = findViewById(R.id.themeButton6)

        // Налаштування відступів системних панелей
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Оновлення візуальних елементів відповідно до теми
        updateBackgroundAndButton()
        updateCircleImages()

        // Налаштування RecyclerView для списку плейлистів
        setupRecyclerView()

        // Налаштування обробників кліків
        setupClickListeners()

        // Запуск спостереження за списком плейлистів
        observePlaylists()

    } // --- Кінець onCreate ---

    // Налаштування RecyclerView
    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.playlistsRecyclerView) // Знаходимо RecyclerView

        // Створюємо адаптер, передаємо лямбди для обробки кліків
        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                // Клік на плейлист -> відкрити PlaylistDetailsActivity
                Log.d("PlaylistDebug", "Clicked on playlist: ${playlist.name}, ID: ${playlist.id}")
                val intent = Intent(this, PlaylistDetailsActivity::class.java)
                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_ID, playlist.id)
                intent.putExtra(PlaylistDetailsActivity.EXTRA_PLAYLIST_NAME, playlist.name)
                startActivity(intent)
            },
            onDeleteClick = { playlist ->
                // Клік на кнопку видалення -> показати діалог підтвердження
                Log.d("PlaylistDebug", "Delete clicked for playlist: ${playlist.name}")
                showDeleteConfirmationDialog(playlist) // Ця функція у нас вже є
            }
        )

        // Встановлюємо адаптер та LayoutManager
        recyclerView.adapter = playlistAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Налаштування обробників кліків для кнопок
    private fun setupClickListeners() {
        // Кнопки зміни теми
        themeToggleButton1.setOnClickListener { toggleTheme() }
        themeToggleButton2.setOnClickListener { toggleTheme() }

        // Кнопка створення плейлиста
        val folderButton: ImageButton = findViewById(R.id.folderButton4)
        folderButton.setOnClickListener {
            Log.d("PlaylistDebug", "Folder button clicked - showing create dialog")
            showCreatePlaylistDialog()
        }

        // Налаштування переходів для інших кнопок
        setupNavigationButtons()
    }

    // Налаштування переходів на інші сторінки (Activity)
    private fun setupNavigationButtons() {
        val buttonActivityPairs = listOf(
            R.id.settingsButton to SettingPage::class.java,
            R.id.searchButton2 to SearchPage::class.java,
            R.id.galleryButton7 to GalleryPage::class.java,
            R.id.notesButton8 to NotesPage::class.java
            // R.id.closeButton1 // Поки не ясно, що робить
            // R.id.likeButton3 // Для майбутнього функціоналу "Улюблене"
            // R.id.cloudappButton5 // Поки не ясно, що робить
        )

        for ((id, activityClass) in buttonActivityPairs) {
            findViewById<ImageButton>(id).setOnClickListener {
                val intent = Intent(this, activityClass)
                startActivity(intent)
            }
        }
        // Обробник для closeButton1, якщо він має просто закривати додаток або повертатись
        findViewById<ImageButton>(R.id.closeButton1)?.setOnClickListener {
            // finish() // Закрити поточну Activity
            // Або інша логіка
            Log.d("AppFlow", "Close button 1 clicked")
        }

    }

    // Спостереження за списком плейлистів та оновлення RecyclerView
    private fun observePlaylists() {
        val recyclerView: RecyclerView? = findViewById(R.id.playlistsRecyclerView) // Робимо nullable на випадок помилки
        val emptyTextView: TextView? = findViewById(R.id.empty_playlists_text)

        if (recyclerView == null || emptyTextView == null) {
            Log.e("PlaylistDebug", "ERROR: Cannot find RecyclerView or empty_playlists_text")
            return
        }

        Log.d("PlaylistDebug", "Setting up RecyclerView playlists observer...")

        playlistsViewModel.allPlaylists.observe(this, Observer { playlists ->
            Log.d("PlaylistDebug", "Observer received update for RecyclerView! Playlists count: ${playlists?.size ?: "null"}")

            // Передаємо список адаптеру
            playlistAdapter.submitList(playlists)

            // Показуємо/ховаємо відповідні елементи
            if (playlists.isNullOrEmpty()) {
                Log.d("PlaylistDebug", "Playlist list IS empty or null. Showing empty text.")
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                Log.d("PlaylistDebug", "Playlist list is NOT empty. Showing RecyclerView.")
                emptyTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })
    }

    // Діалог створення плейлиста (з обмеженням)
    private fun showCreatePlaylistDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Створити новий плейлист")

        val inputLayout = TextInputLayout(this).apply {
            setPadding(50, 30, 50, 30)
            hint = "Назва плейлиста"
        }
        val input = EditText(this)
        inputLayout.addView(input)
        builder.setView(inputLayout)

        builder.setPositiveButton("Створити") { dialog, _ ->
            val playlistName = input.text.toString().trim()
            if (playlistName.isNotEmpty()) {
                val currentPlaylists = playlistsViewModel.allPlaylists.value
                val maxPlaylists = 5 // Ліміт

                if (currentPlaylists != null && currentPlaylists.size >= maxPlaylists) {
                    Toast.makeText(this, "Досягнуто ліміту ($maxPlaylists плейлистів)", Toast.LENGTH_SHORT).show()
                } else {
                    playlistsViewModel.createPlaylist(playlistName)
                    Toast.makeText(this, "Плейлист '$playlistName' створено", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Назва не може бути порожньою", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Скасувати") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Діалог підтвердження видалення плейлиста
    private fun showDeleteConfirmationDialog(playlist: Playlist) {
        AlertDialog.Builder(this)
            .setTitle("Видалити плейлист?")
            .setMessage("Ви впевнені, що хочете видалити плейлист \"${playlist.name}\"?")
            .setPositiveButton("Видалити") { _, _ ->
                playlistsViewModel.deletePlaylist(playlist)
                Toast.makeText(this, "Плейлист '${playlist.name}' видалено", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // --- Функції для теми ---
    private fun updateBackgroundAndButton() {
        val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        mainLayout.setBackgroundResource(if (isNight) R.drawable.fondark else R.drawable.fonday)
        // Іконки кнопок теми оновлюються в updateCircleImages
    }

    private fun toggleTheme() {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        recreate() // Перезапуск Activity для застосування теми
    }

    private fun updateCircleImages() {
        val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val resMap = mapOf(
            R.id.closeButton1 to if (isNight) R.drawable.close_night else R.drawable.close,
            R.id.searchButton2 to if (isNight) R.drawable.search_night else R.drawable.search,
            R.id.likeButton3 to if (isNight) R.drawable.like_night else R.drawable.like,
            R.id.folderButton4 to if (isNight) R.drawable.folder_night else R.drawable.folder,
            R.id.cloudappButton5 to if (isNight) R.drawable.cloudapp_night else R.drawable.cloudapp,
            R.id.themeButton6 to if (isNight) R.drawable.day_night else R.drawable.night, // Тема (альтернативна)
            R.id.galleryButton7 to if (isNight) R.drawable.gallery_night else R.drawable.gallery,
            R.id.notesButton8 to if (isNight) R.drawable.notes_night else R.drawable.notes,
            R.id.settingsButton to if (isNight) R.drawable.settings_night else R.drawable.settings,
            R.id.themeButton to if (isNight) R.drawable.night else R.drawable.cloud // Тема (основна)
            // R.id.arrow - оновлюється в observePlaylists, якщо він використовується
        )
        resMap.forEach { (id, resId) ->
            findViewById<ImageButton>(id)?.setImageResource(resId) // Додав ?. для безпеки
        }
    }
    // --- Кінець функцій для теми ---

}
