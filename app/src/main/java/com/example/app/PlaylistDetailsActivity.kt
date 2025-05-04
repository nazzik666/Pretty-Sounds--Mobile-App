package com.example.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.data.AppDatabase
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist // Потрібно для діалогу видалення
import com.example.app.models.Sound
import com.example.app.ui.playlists.PlaylistDetailsViewModel
import com.example.app.ui.playlists.PlaylistSoundAdapter
import com.example.app.ui.playlists.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlaylistDetailsActivity : AppCompatActivity() {

    // ViewModel для деталей плейлиста
    private val viewModel: PlaylistDetailsViewModel by viewModels {
        val repository = PlaylistRepository(AppDatabase.getDatabase(this).playlistDao())
        ViewModelFactory(repository)
    }

    private lateinit var soundAdapter: PlaylistSoundAdapter
    private var currentPlaylistId: Int = -1 // Зберігаю ID поточного плейлиста
    private var currentPlaylistName: String? = null // Зберігаю Назву

    // Ключі для передачі даних через Intent
    companion object {
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"
        const val EXTRA_PLAYLIST_NAME = "extra_playlist_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_details)

        // Отримую ID та назву плейлиста з Intent
        currentPlaylistId = intent.getIntExtra(EXTRA_PLAYLIST_ID, -1)
        currentPlaylistName = intent.getStringExtra(EXTRA_PLAYLIST_NAME)

        val titleTextView: TextView = findViewById(R.id.playlist_details_title)
        titleTextView.text = currentPlaylistName ?: "Плейлист" // Встановлюю заголовок

        if (currentPlaylistId == -1) {
            // Якщо ID не передано, показую помилку і закриваю Activity
            Toast.makeText(this, "Помилка: ID плейлиста не знайдено", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        observeViewModel()

        // Завантажую дані для цього плейлиста
        viewModel.loadPlaylist(currentPlaylistId)

        // Кнопка для додавання звуків
        val fabAddSound: FloatingActionButton = findViewById(R.id.fab_add_sound_to_playlist)
        fabAddSound.setOnClickListener {
            // Тут має бути логіка відкриття екрану/діалогу вибору звуків
            // Наприклад, перехід на GalleryPage або показ діалогу зі списком звуків
            showAddSoundDialog() // Поки що простий діалог
        }
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.soundsRecyclerView)
        soundAdapter = PlaylistSoundAdapter(
            onSoundClicked = { sound ->
                // Обробка кліку на звук (наприклад, почати відтворення)
                Toast.makeText(this, "Відтворення: ${sound.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClicked = { sound ->
                // Видалення звуку з ПОТОЧНОГО плейлиста
                viewModel.removeSoundFromCurrentPlaylist(sound.id)
                Toast.makeText(this, "Видалено: ${sound.name}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = soundAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        // Спостерігаю за списком звуків у плейлисті
        viewModel.soundsInPlaylist.observe(this, Observer { sounds ->
            soundAdapter.submitList(sounds)
            // Можна показувати/ховати текст "Плейлист порожній"
            val emptyTextView: TextView = findViewById(R.id.empty_sounds_text)
            emptyTextView.visibility = if (sounds.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    // Діалог для додавання звуків (дуже спрощений приклад)
    private fun showAddSoundDialog() {
        // Отримую всі доступні звуки
        // Використовую простіший синтаксис лямбда для Observer
        viewModel.allSounds.observe(this) { allSounds ->
            // Лямбда автоматично отримує allSounds як List<Sound>?

            // Важливо: Цей код буде виконуватись КОЖЕН РАЗ, коли LiveData allSounds оновлюється.
            // Для діалогу, який має показатись один раз, це може бути не ідеально.

            if (allSounds == null || allSounds.isEmpty()) {
                Toast.makeText(
                    this@PlaylistDetailsActivity,
                    "Немає доступних звуків для додавання",
                    Toast.LENGTH_SHORT
                ).show()
                // Якщо ми всередині лямбди, треба вийти з неї
                return@observe
            }

            // Отримую список назв звуків
            val soundNames = allSounds.map { it.name }.toTypedArray()
            // Зберігаю відповідні ID
            val soundIds = allSounds.map { it.id }.toTypedArray()
            val selectedSounds = BooleanArray(soundNames.size) // Для multi-choice

            // Будую і показую діалог
            AlertDialog.Builder(this@PlaylistDetailsActivity)
                .setTitle("Додати звуки до плейлиста")
                .setMultiChoiceItems(soundNames, selectedSounds) { _, which, isChecked ->
                    // Відмічаю вибрані/невибрані звуки
                    selectedSounds[which] = isChecked
                }
                .setPositiveButton("Додати") { _, _ ->
                    var addedCount = 0 // Лічильник доданих звуків
                    for (i in selectedSounds.indices) {
                        if (selectedSounds[i]) {
                            // Викликаю метод ViewModel для додавання звуку до ПОТОЧНОГО плейлиста
                            viewModel.addSoundToCurrentPlaylist(soundIds[i])
                            addedCount++
                        }
                    }
                    if (addedCount > 0) {
                        Toast.makeText(this, "Додано $addedCount звуків", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Скасувати", null)
                .show()
        } // Кінець лямбди observe
    }
}