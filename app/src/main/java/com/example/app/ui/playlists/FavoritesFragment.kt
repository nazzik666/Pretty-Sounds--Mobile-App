package com.example.app.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.data.AppDatabase
import com.example.app.data.PlaylistRepository
import com.example.app.models.Sound

class FavoritesFragment : Fragment() {

    // ViewModel для цього фрагмента
    private val viewModel: FavoritesViewModel by viewModels {
        val repository = PlaylistRepository(AppDatabase.getDatabase(requireContext()).playlistDao())
        ViewModelFactory(repository)
    }

    // Адаптери (перевикористовуємо існуючі для простоти)
    private lateinit var favPlaylistAdapter: PlaylistAdapter
    private lateinit var favSoundAdapter: PlaylistSoundAdapter // Потрібно створити цей файл/клас

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPlaylistsRecyclerView(view)
        setupSoundsRecyclerView(view)
        observeViewModel(view)
    }

    // Налаштування RecyclerView для улюблених плейлистів
    private fun setupPlaylistsRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.fav_playlists_recycler_view)
        // Перевикористовуємо PlaylistAdapter, передаючи лямбди для видалення з улюбленого
        favPlaylistAdapter = PlaylistAdapter(
            onPlaylistDeleteClick = { playlist ->
                // Видалення плейлиста з УЛЮБЛЕНОГО (знімаємо зірочку)
                viewModel.removePlaylistFromFavorites(playlist)
                Toast.makeText(requireContext(), "'${playlist.name}' видалено з улюбленого", Toast.LENGTH_SHORT).show()
            },
            onPlaylistAddSoundClick = { /* На екрані улюбленого нічого не робимо */ },
            onPlaylistFavoriteClick = { playlist ->
                // Клік на зірочку тут ТАКОЖ видаляє з улюбленого
                viewModel.removePlaylistFromFavorites(playlist)
                Toast.makeText(requireContext(), "'${playlist.name}' видалено з улюбленого", Toast.LENGTH_SHORT).show()
            },
            onPlaylistToggleExpandClick = { /* Розгортання тут не потрібне */ },

            // <-- ДОДАЄМО ВІДСУТНІЙ ПАРАМЕТР -->
            onPlaylistRenameClick = { playlist ->
                // Перейменування з екрану улюбленого не передбачено
                Toast.makeText(requireContext(), "Перейменування доступне на головному екрані", Toast.LENGTH_SHORT).show()
            },
            // ------------------------------------

            onSoundFavoriteClick = { _, _ -> /* Не використовується для списку плейлистів */ },
            onSoundDeleteClick = { _, _ -> /* Не використовується для списку плейлистів */ }
        )
        recyclerView.adapter = favPlaylistAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // Налаштування RecyclerView для улюблених звуків
    private fun setupSoundsRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.fav_sounds_recycler_view)
        // Використовуємо PlaylistSoundAdapter, передаючи лямбди для видалення з улюбленого
        favSoundAdapter = PlaylistSoundAdapter(
            onSoundClicked = { sound ->
                // Можна додати програвання
                Toast.makeText(requireContext(), "Play: ${sound.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClicked = { sound ->
                viewModel.removeSoundFromFavorites(sound) // Видаляємо з улюбленого
            }
        )
        recyclerView.adapter = favSoundAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    // Спостереження за LiveData
    private fun observeViewModel(view: View) {
        val favPlaylistsHeader: TextView = view.findViewById(R.id.fav_playlists_header)
        val favPlaylistsRecyclerView: RecyclerView = view.findViewById(R.id.fav_playlists_recycler_view)
        val emptyFavPlaylistsText: TextView = view.findViewById(R.id.empty_fav_playlists_text)

        val favSoundsHeader: TextView = view.findViewById(R.id.fav_sounds_header)
        val favSoundsRecyclerView: RecyclerView = view.findViewById(R.id.fav_sounds_recycler_view)
        val emptyFavSoundsText: TextView = view.findViewById(R.id.empty_fav_sounds_text)

        // Спостерігаємо за favoritePlaylistItems
        viewModel.favoritePlaylistItems.observe(viewLifecycleOwner) { items ->
            // 'items' - це вже готовий змішаний список List<PlaylistItemType>
            favPlaylistAdapter.submitList(items) // Передаємо його адаптеру

            // Керування видимістю секції плейлистів
            // Перевіряємо, чи є в списку хоча б один заголовок
            val hasPlaylists = items.any { it is PlaylistItemType.PlaylistHeader }
            favPlaylistsHeader.visibility = if (hasPlaylists) View.VISIBLE else View.GONE
            favPlaylistsRecyclerView.visibility = if (hasPlaylists) View.VISIBLE else View.GONE
            emptyFavPlaylistsText.visibility = if (hasPlaylists) View.GONE else View.VISIBLE
        }

        // Спостерігаємо за улюбленими звуками
        viewModel.favoriteSounds.observe(viewLifecycleOwner) { sounds ->
            favSoundAdapter.submitList(sounds)
            // Керування видимістю секції звуків
            val hasSounds = !sounds.isNullOrEmpty()
            favSoundsHeader.visibility = if (hasSounds) View.VISIBLE else View.GONE
            favSoundsRecyclerView.visibility = if (hasSounds) View.VISIBLE else View.GONE
            emptyFavSoundsText.visibility = if (hasSounds) View.GONE else View.VISIBLE
        }
    }
}