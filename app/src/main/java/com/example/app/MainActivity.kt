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
import androidx.lifecycle.Observer // Імпорт Observer з lifecycle
import androidx.recyclerview.widget.LinearLayoutManager // Для RecyclerView
import androidx.recyclerview.widget.RecyclerView // Для RecyclerView
import com.example.app.data.AppDatabase
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist
import com.example.app.models.Sound // Імпорт Sound
import com.example.app.ui.playlists.PlaylistAdapter // Адаптер для списку плейлистів/звуків
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

    }

    // Налаштування RecyclerView
    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.playlistsRecyclerView)

        playlistAdapter = PlaylistAdapter(
            onPlaylistDeleteClick = { playlist ->
                showDeleteConfirmationDialog(playlist)
            },
            onPlaylistAddSoundClick = { playlist ->
                showAddSoundDialog(playlist)
            },
            onPlaylistFavoriteClick = { playlist ->
                Log.d("MainActivity", "Favorite clicked for playlist: ${playlist.name}")
                playlistsViewModel.togglePlaylistFavoriteStatus(playlist) // Викликаю метод ViewModel
            },
            onPlaylistToggleExpandClick = { playlist ->
                playlistsViewModel.togglePlaylistExpansion(playlist.id)
            },
            onPlaylistRenameClick = { playlist ->
                Log.d("MainActivity", "Rename clicked for playlist: ${playlist.name}")
                showRenamePlaylistDialog(playlist) // Викликаю новий діалог
            },
            onSoundFavoriteClick = { sound, isCurrentlyFavorite -> // <-- Обробник для Улюбленого звуку
                Log.d("MainActivity", "Favorite clicked for sound: ${sound.name}")
                playlistsViewModel.toggleFavoriteStatus(sound)
            },
            onSoundDeleteClick = { sound, playlistId -> // <-- Обробник для кошика звуку
                Log.d("MainActivity", "Delete clicked for sound: ${sound.name} from playlist ID: $playlistId")
                playlistsViewModel.removeSoundFromPlaylist(sound.id, playlistId)
                Toast.makeText(this,"Видалено '${sound.name}'", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = playlistAdapter
        recyclerView.layoutManager = LinearLayoutManager(this) // Вертикальний список
    }

    // Налаштування обробників кліків для кнопок (крім тих, що в RecyclerView)
    private fun setupClickListeners() {
        // Кнопки зміни теми
        themeToggleButton1.setOnClickListener { toggleTheme() }
        themeToggleButton2.setOnClickListener { toggleTheme() }

        // Кнопка створення плейлиста (папка)
        val folderButton: ImageButton = findViewById(R.id.folderButton4)
        folderButton.setOnClickListener {
            Log.d("PlaylistDebug", "Folder button clicked - showing create dialog")
            showCreatePlaylistDialog()
        }

        // Налаштування переходів для інших навігаційних кнопок
        setupNavigationButtons()
    }

    // Налаштування переходів на інші сторінки (Activity)
    private fun setupNavigationButtons() {
        val buttonActivityPairs = listOf(
            R.id.settingsButton to SettingPage::class.java,
            R.id.searchButton2 to SearchPage::class.java,
            R.id.galleryButton7 to GalleryPage::class.java,
            R.id.notesButton8 to NotesPage::class.java
        )

        for ((id, activityClass) in buttonActivityPairs) {
            // Перевіряємо, чи кнопка існує, перш ніж встановити слухач
            findViewById<ImageButton>(id)?.setOnClickListener {
                val intent = Intent(this, activityClass)
                startActivity(intent)
            } ?: Log.w("SetupNav", "Button with ID $id not found")
        }

        findViewById<ImageButton>(R.id.closeButton1)?.setOnClickListener {
            Log.d("AppFlow", "Close button 1 clicked")
            // finish() // Можливо, закрити додаток?
        }
        findViewById<ImageButton>(R.id.likeButton3)?.setOnClickListener {
            Log.d("Navigation", "Like button clicked, starting FavoritesActivity")
            // Запускаємо FavoritesActivity через Intent
            val intent = Intent(this, FavoritesActivity::class.java)
            startActivity(intent)
        }
    }

    // Спостереження за змішаним списком (плейлисти + звуки) та оновлення RecyclerView
    private fun observePlaylists() {
        val recyclerView: RecyclerView? = findViewById(R.id.playlistsRecyclerView)
        val emptyTextView: TextView? = findViewById(R.id.empty_playlists_text)

        if (recyclerView == null || emptyTextView == null) {
            Log.e("PlaylistDebug", "ERROR: Cannot find RecyclerView or empty_playlists_text")
            return
        }

        Log.d("PlaylistDebug", "Setting up MIXED list observer (Headers + Sounds)...")

        // Спостерігаємо за LiveData 'playlistItems' з ViewModel
        playlistsViewModel.playlistItems.observe(this, Observer { items ->
            Log.d("PlaylistDebug", "Observer received update for MIXED list! Items count: ${items?.size ?: "null"}")

            // Передаємо змішаний список адаптеру
            playlistAdapter.submitList(items)

            // Показуємо/ховаємо відповідні елементи
            if (items.isNullOrEmpty()) {
                Log.d("PlaylistDebug", "Mixed list IS empty or null. Showing empty text.")
                emptyTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                Log.d("PlaylistDebug", "Mixed list is NOT empty. Showing RecyclerView.")
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
                // Використовуємо rawPlaylists для перевірки ліміту
                val currentPlaylists = playlistsViewModel.rawPlaylists.value
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

    // Діалог для перейменування плейлиста
    private fun showRenamePlaylistDialog(playlist: Playlist) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Перейменувати плейлист")

        // Створюємо поле вводу і встановлюємо поточну назву
        val inputLayout = TextInputLayout(this).apply {
            setPadding(50, 30, 50, 30)
            hint = "Нова назва"
        }
        val input = EditText(this).apply {
            setText(playlist.name) // Встановлюємо поточну назву в поле вводу
            setSelection(playlist.name.length) // Ставимо курсор в кінець тексту
        }
        inputLayout.addView(input)
        builder.setView(inputLayout)

        // Кнопка "Зберегти"
        builder.setPositiveButton("Зберегти") { dialog, _ ->
            val newName = input.text.toString().trim()
            // Перевіряємо, чи назва не порожня і чи вона змінилася
            if (newName.isNotEmpty() && newName != playlist.name) {
                // Викликаємо метод ViewModel для оновлення назви
                playlistsViewModel.updatePlaylistName(playlist.id, newName)
                Toast.makeText(this, "Плейлист перейменовано на '$newName'", Toast.LENGTH_SHORT).show()
            } else if (newName.isEmpty()) {
                Toast.makeText(this, "Назва не може бути порожньою", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss() // Закриваємо діалог
        }
        // Кнопка "Скасувати"
        builder.setNegativeButton("Скасувати") { dialog, _ ->
            dialog.cancel()
        }

        builder.show() // Показуємо діалог
    }

    // Діалог для додавання ЗВУКІВ до КОНКРЕТНОГО плейлиста
    private fun showAddSoundDialog(targetPlaylist: Playlist) {
        Log.d("AddSoundDialog", "Showing dialog for playlist: ${targetPlaylist.name}")

        // Тепер отримуємо LiveData зі звуками через ViewModel (ПУБЛІЧНЕ ПОЛЕ)
        val allSoundsLiveData = playlistsViewModel.allSounds // <-- ВИПРАВЛЕНО: Використовуємо публічне поле

        allSoundsLiveData.observe(this) { allSounds -> // <-- ВИПРАВЛЕНО: Лямбда-синтаксис
            // Видаляємо спостерігач одразу, щоб діалог не з'являвся повторно
            if (allSounds != null) {
                allSoundsLiveData.removeObservers(this)
                Log.d("AddSoundDialog", "Received ${allSounds.size} sounds. Removing observer.")
            } else {
                Log.d("AddSoundDialog", "Received null sound list.")
                return@observe
            }

            if (allSounds.isEmpty()) {
                Toast.makeText(this@MainActivity, "Немає доступних звуків для додавання", Toast.LENGTH_SHORT).show()
                return@observe
            }

            // Логіка побудови діалогу (без змін)
            val soundNames = allSounds.map { it.name }.toTypedArray()
            val soundIds = allSounds.map { it.id }.toTypedArray()
            val selectedSounds = BooleanArray(soundNames.size)

            AlertDialog.Builder(this@MainActivity)
                .setTitle("Додати до '${targetPlaylist.name}'")
                .setMultiChoiceItems(soundNames, selectedSounds) { _, which, isChecked ->
                    selectedSounds[which] = isChecked
                }
                .setPositiveButton("Додати") { _, _ ->
                    val soundsToAdd = mutableListOf<String>()
                    for (i in selectedSounds.indices) {
                        if (selectedSounds[i]) {
                            soundsToAdd.add(soundIds[i])
                        }
                    }
                    if (soundsToAdd.isNotEmpty()) {
                        Log.d("AddSoundDialog", "Adding sound IDs: $soundsToAdd to playlist ${targetPlaylist.id}")
                        playlistsViewModel.addSoundsToPlaylist(soundsToAdd, targetPlaylist.id) // Викликаємо метод ViewModel
                        Toast.makeText(this@MainActivity, "Додано ${soundsToAdd.size} звуків до '${targetPlaylist.name}'", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("AddSoundDialog", "No sounds selected to add.")
                    }
                }
                .setNegativeButton("Скасувати", null)
                .show()
            // Кінець логіки діалогу

        } // <-- Кінець лямбди observe
    }


    // --- Функції для теми ---
    private fun updateBackgroundAndButton() {
        val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        mainLayout.setBackgroundResource(if (isNight) R.drawable.fondark else R.drawable.fonday)
    }

    private fun toggleTheme() {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        )
        recreate()
    }

    private fun updateCircleImages() {
        val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val resMap = mapOf(
            R.id.closeButton1 to if (isNight) R.drawable.close_night else R.drawable.close,
            R.id.searchButton2 to if (isNight) R.drawable.search_night else R.drawable.search,
            R.id.likeButton3 to if (isNight) R.drawable.like_night else R.drawable.like,
            R.id.folderButton4 to if (isNight) R.drawable.folder_night else R.drawable.folder,
            R.id.cloudappButton5 to if (isNight) R.drawable.cloudapp_night else R.drawable.cloudapp,
            R.id.themeButton6 to if (isNight) R.drawable.day_night else R.drawable.night,
            R.id.galleryButton7 to if (isNight) R.drawable.gallery_night else R.drawable.gallery,
            R.id.notesButton8 to if (isNight) R.drawable.notes_night else R.drawable.notes,
            R.id.settingsButton to if (isNight) R.drawable.settings_night else R.drawable.settings,
            R.id.themeButton to if (isNight) R.drawable.night else R.drawable.cloud
        )
        resMap.forEach { (id, resId) ->
            findViewById<ImageButton>(id)?.setImageResource(resId)
        }
    }
}