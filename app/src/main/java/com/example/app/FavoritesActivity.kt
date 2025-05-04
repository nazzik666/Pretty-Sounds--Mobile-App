package com.example.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels // Імпорт для Activity
import androidx.appcompat.app.AppCompatActivity // Батьківський клас Activity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.data.AppDatabase
import com.example.app.data.PlaylistRepository
import com.example.app.models.Sound // Імпорт Sound
import com.example.app.ui.playlists.FavoritesViewModel // Наш ViewModel
import com.example.app.ui.playlists.PlaylistAdapter // Перевикористовуємо
import com.example.app.ui.playlists.PlaylistItemType // Потрібен для перетворення
import com.example.app.ui.playlists.PlaylistSoundAdapter // Перевикористовуємо
import com.example.app.ui.playlists.ViewModelFactory // Наша фабрика

class FavoritesActivity : AppCompatActivity() {

    // ViewModel для цієї Activity
    private val viewModel: FavoritesViewModel by viewModels {
        val repository = PlaylistRepository(AppDatabase.getDatabase(this).playlistDao()) // Використовуємо this
        ViewModelFactory(repository)
    }

    // Адаптери
    private lateinit var favPlaylistAdapter: PlaylistAdapter
    private lateinit var favSoundAdapter: PlaylistSoundAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites) // Встановлюємо макет activity_favorites.xml

        // Знаходимо View тут, в onCreate
        val playlistsRecyclerView: RecyclerView = findViewById(R.id.fav_playlists_recycler_view)
        val soundsRecyclerView: RecyclerView = findViewById(R.id.fav_sounds_recycler_view)

        setupPlaylistsRecyclerView(playlistsRecyclerView) // Передаємо RecyclerView
        setupSoundsRecyclerView(soundsRecyclerView)     // Передаємо RecyclerView
        observeViewModel() // Запускаємо спостереження
    }

    // Налаштування RecyclerView для улюблених плейлистів
    private fun setupPlaylistsRecyclerView(recyclerView: RecyclerView) {
        // Перевикористовуємо PlaylistAdapter
        favPlaylistAdapter = PlaylistAdapter(
            onPlaylistDeleteClick = { playlist -> // Клік на кошик біля плейлиста
                viewModel.removePlaylistFromFavorites(playlist)
                Toast.makeText(this, "'${playlist.name}' видалено з улюбленого", Toast.LENGTH_SHORT).show()
            },
            onPlaylistAddSoundClick = { /* Нічого на цьому екрані */ },
            onPlaylistFavoriteClick = { playlist -> // Клік на зірочку плейлиста
                viewModel.removePlaylistFromFavorites(playlist) // Теж видаляє з улюбленого
                Toast.makeText(this, "'${playlist.name}' видалено з улюбленого", Toast.LENGTH_SHORT).show()
            },
            onPlaylistToggleExpandClick = { playlist -> // <-- ТЕПЕР РЕАЛІЗОВАНО
                viewModel.toggleFavoritePlaylistExpansion(playlist.id) // Викликаємо ViewModel
            },
            onPlaylistRenameClick = { /* Перейменування тут недоступне */ },
            onSoundFavoriteClick = { sound, _ -> // Клік на зірочку ЗВУКУ всередині плейлиста
                viewModel.toggleSoundFavoriteStatus(sound) // Змінюємо статус улюбленого для звуку
            },
            onSoundDeleteClick = { sound, playlistId -> // Клік на кошик ЗВУКУ всередині плейлиста
                // На екрані улюбленого видалення звуку з плейлиста, мабуть, не логічне?
                // Можливо, варто просто видаляти звук з улюбленого?
                viewModel.removeSoundFromFavorites(sound)
                Toast.makeText(this,"'${sound.name}' видалено з улюбленого", Toast.LENGTH_SHORT).show()
                // Або якщо таки треба видаляти з плейлиста:
                // viewModel.removeSoundFromPlaylist(sound.id, playlistId) // Потрібен метод у FavoritesViewModel
            }
        )
        recyclerView.adapter = favPlaylistAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // Налаштування RecyclerView для улюблених звуків
    private fun setupSoundsRecyclerView(recyclerView: RecyclerView) { // Приймає RecyclerView
        favSoundAdapter = PlaylistSoundAdapter(
            onSoundClicked = { sound ->
                // TODO: Додати програвання звуку
                Toast.makeText(this, "Play: ${sound.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClicked = { sound -> // Клік на кошик біля звуку
                viewModel.removeSoundFromFavorites(sound) // Видаляємо з улюбленого
                Toast.makeText(this, "'${sound.name}' видалено з улюбленого", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = favSoundAdapter
        recyclerView.layoutManager = LinearLayoutManager(this) // Використовуємо this
    }

    // Спостереження за LiveData
    private fun observeViewModel() {
        val favPlaylistsHeader: TextView = findViewById(R.id.fav_playlists_header)
        val favPlaylistsRecyclerView: RecyclerView = findViewById(R.id.fav_playlists_recycler_view)
        val emptyFavPlaylistsText: TextView = findViewById(R.id.empty_fav_playlists_text)

        val favSoundsHeader: TextView = findViewById(R.id.fav_sounds_header)
        val favSoundsRecyclerView: RecyclerView = findViewById(R.id.fav_sounds_recycler_view)
        val emptyFavSoundsText: TextView = findViewById(R.id.empty_fav_sounds_text)

        // Спостерігаємо за ЗМІШАНИМ списком улюблених плейлистів
        viewModel.favoritePlaylistItems.observe(this) { items -> // <-- ЗМІНЕНО ТУТ
            favPlaylistAdapter.submitList(items) // Передаємо змішаний список

            // Керування видимістю секції плейлистів
            // Перевіряємо, чи є в списку хоча б один заголовок (тобто хоч один плейлист)
            val hasPlaylists = items.any { it is PlaylistItemType.PlaylistHeader }
            favPlaylistsHeader.visibility = if (hasPlaylists) View.VISIBLE else View.GONE
            favPlaylistsRecyclerView.visibility = if (hasPlaylists) View.VISIBLE else View.GONE
            emptyFavPlaylistsText.visibility = if (hasPlaylists) View.GONE else View.VISIBLE
        }

        // Спостерігаємо за улюбленими звуками (без змін)
        viewModel.favoriteSounds.observe(this) { sounds ->
            favSoundAdapter.submitList(sounds)
            // Керування видимістю секції звуків (без змін)
            val hasSounds = !sounds.isNullOrEmpty()
            favSoundsHeader.visibility = if (hasSounds) View.VISIBLE else View.GONE
            favSoundsRecyclerView.visibility = if (hasSounds) View.VISIBLE else View.GONE
            emptyFavSoundsText.visibility = if (hasSounds) View.GONE else View.VISIBLE
        }
    }
}