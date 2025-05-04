package com.example.app.ui.playlists

import androidx.lifecycle.*
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist
import com.example.app.models.Sound
import kotlinx.coroutines.launch

// ViewModel для екрану деталей плейлиста
class PlaylistDetailsViewModel(private val repository: PlaylistRepository) : ViewModel() {

    // LiveData для зберігання ID поточного плейлиста
    private val _playlistId = MutableLiveData<Int>()

    // LiveData для зберігання інформації про поточний плейлист
    private val _playlist = _playlistId.switchMap { id ->
        liveData { emit(repository.getPlaylistById(id)) } // Завантажую плейлист за ID
    }
    val playlist: LiveData<Playlist?> = _playlist // Публічна LiveData

    // LiveData для зберігання списку звуків у поточному плейлисті
    // switchMap реагує на зміни _playlist і завантажує відповідні звуки
    val soundsInPlaylist: LiveData<List<Sound>> = _playlist.switchMap { currentPlaylist ->
        liveData {
            if (currentPlaylist != null && currentPlaylist.soundIds.isNotEmpty()) {
                // Якщо плейлист існує і має звуки, завантажую їх
                emit(repository.getSoundsForPlaylist(currentPlaylist.soundIds))
            } else {
                // Інакше повертаю порожній список
                emit(emptyList<Sound>())
            }
        }
    }

    // LiveData для списку ВСІХ звуків (для діалогу додавання)
    val allSounds: LiveData<List<Sound>> = repository.allSounds

    // Функція для встановлення ID плейлиста, який потрібно відобразити
    fun loadPlaylist(id: Int) {
        _playlistId.value = id
    }

    // Функція для видалення звуку з поточного плейлиста
    fun removeSoundFromCurrentPlaylist(soundId: String) {
        _playlistId.value?.let { currentPlaylistId -> // Перевіряю, що ID плейлиста встановлено
            viewModelScope.launch {
                repository.removeSoundFromPlaylist(soundId, currentPlaylistId)
                // LiveData (_playlist та soundsInPlaylist) оновляться автоматично завдяки switchMap
            }
        }
    }

    // Функція для додавання звуку до вказаного плейлиста
    fun addSoundToSpecificPlaylist(soundId: String, playlistId: Int) {
        viewModelScope.launch {
            repository.addSoundsToPlaylist(listOf(soundId), playlistId)
        }
    }

    // Функція для додавання звуку до ПОТОЧНОГО плейлиста, що відображається в цій ViewModel
    fun addSoundToCurrentPlaylist(soundId: String) {
        _playlistId.value?.let { currentPlaylistId ->
            viewModelScope.launch {
                repository.addSoundsToPlaylist(listOf(soundId), currentPlaylistId)
            }
        }
    }
}