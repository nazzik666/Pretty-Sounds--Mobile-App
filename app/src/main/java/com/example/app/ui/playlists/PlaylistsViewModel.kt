package com.example.app.ui.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist
import kotlinx.coroutines.launch

// ViewModel для екрану списку плейлистів
class PlaylistsViewModel(private val repository: PlaylistRepository) : ViewModel() {

    // LiveData для спостереження за списком всіх плейлистів з репозиторію
    val allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists

    // Функція для створення нового плейлиста
    fun createPlaylist(name: String) {
        // Запускаю корутину для виконання операції вставки в фоновому потоці
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    // Функція для видалення плейлиста
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
}