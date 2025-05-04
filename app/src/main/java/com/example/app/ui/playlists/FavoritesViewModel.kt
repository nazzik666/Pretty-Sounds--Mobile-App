package com.example.app.ui.playlists

import android.util.Log
import androidx.lifecycle.*
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist
import com.example.app.models.Sound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesViewModel(private val repository: PlaylistRepository) : ViewModel() {

    // Базові LiveData з улюбленим
    private val _favoritePlaylists: LiveData<List<Playlist>> = repository.favoritePlaylists
    val favoriteSounds: LiveData<List<Sound>> = repository.favoriteSounds // Звуки поки окремо

    // Стан розгорнутості для УЛЮБЛЕНИХ плейлистів
    private val favExpansionState = MutableLiveData<MutableMap<Int, Boolean>>(mutableMapOf())

    // Функція для перемикання стану розгорнутості улюбленого плейлиста
    fun toggleFavoritePlaylistExpansion(playlistId: Int) {
        val currentMap = favExpansionState.value ?: mutableMapOf()
        val currentState = currentMap[playlistId] ?: false
        currentMap[playlistId] = !currentState
        favExpansionState.value = currentMap
        Log.d("FavViewModel", "Toggled expansion for $playlistId to ${!currentState}")
    }

    // MediatorLiveData для формування змішаного списку УЛЮБЛЕНИХ плейлистів
    val favoritePlaylistItems: LiveData<List<PlaylistItemType>> = MediatorLiveData<List<PlaylistItemType>>().apply {
        addSource(_favoritePlaylists) { playlists ->
            viewModelScope.launch(Dispatchers.IO) {
                val state = favExpansionState.value ?: mutableMapOf()
                val newItems = buildMixedFavoriteList(playlists, state)
                withContext(Dispatchers.Main) { value = newItems }
            }
        }
        addSource(favExpansionState) { state ->
            viewModelScope.launch(Dispatchers.IO) {
                val playlists = _favoritePlaylists.value ?: emptyList()
                val newItems = buildMixedFavoriteList(playlists, state)
                withContext(Dispatchers.Main) { value = newItems }
            }
        }
    }

    // Допоміжна функція для побудови змішаного списку УЛЮБЛЕНОГО
    private suspend fun buildMixedFavoriteList(
        playlists: List<Playlist>,
        expansionMap: Map<Int, Boolean>
    ): List<PlaylistItemType> {
        val items = mutableListOf<PlaylistItemType>()
        playlists.forEach { playlist ->
            // Перевіряємо, чи плейлист дійсно улюблений (про всяк випадок)
            if (playlist.isFavorite) {
                val isExpanded = expansionMap[playlist.id] ?: false
                items.add(PlaylistItemType.PlaylistHeader(playlist, isExpanded))
                if (isExpanded) {
                    // Завантажуємо ВСІ звуки цього плейлиста (не тільки улюблені звуки)
                    val sounds = repository.getSoundsForPlaylist(playlist.id)
                    sounds.forEach { sound ->
                        items.add(PlaylistItemType.SoundItem(sound, playlist.id))
                    }
                }
            }
        }
        Log.d("FavViewModel", "Built favorite mixed list with ${items.size} items.")
        return items
    }


    // Функції для видалення з улюбленого
    fun removePlaylistFromFavorites(playlist: Playlist) {
        viewModelScope.launch {
            repository.setPlaylistFavoriteStatus(playlist.id, false)
            // Стан розгорнутості автоматично видалиться при оновленні _favoritePlaylists
            // Але можна і вручну, якщо будуть проблеми:
            // val currentMap = favExpansionState.value ?: mutableMapOf()
            // if (currentMap.remove(playlist.id) != null) { favExpansionState.postValue(currentMap) }
        }
    }

    fun removeSoundFromFavorites(sound: Sound) {
        viewModelScope.launch {
            repository.setSoundFavoriteStatus(sound.id, false)
            // Оновлення favoriteSounds відбудеться автоматично
        }
    }

    // Функція для перемикання статусу звуку (знадобиться для адаптера)
    fun toggleSoundFavoriteStatus(sound: Sound) {
        viewModelScope.launch {
            val newState = !sound.isFavorite
            repository.setSoundFavoriteStatus(sound.id, newState)
        }
    }
}