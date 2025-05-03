package com.example.app.ui.playlists

import android.util.Log // Імпорт логування
import androidx.lifecycle.*
import com.example.app.data.PlaylistRepository
import com.example.app.models.Playlist
import com.example.app.models.Sound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistsViewModel(private val repository: PlaylistRepository) : ViewModel() {

    // Базова LiveData зі списком тільки плейлистів
    private val _allPlaylists: LiveData<List<Playlist>> = repository.allPlaylists
    // Публічна LiveData тільки з плейлистами (для перевірки ліміту)
    val rawPlaylists: LiveData<List<Playlist>> = _allPlaylists
    // Публічна LiveData зі списком всіх звуків (для діалогу додавання)
    val allSounds: LiveData<List<Sound>> = repository.allSounds

    // Стан розгорнутості плейлистів (Playlist ID -> isExpanded?)
    private val expansionState = MutableLiveData<MutableMap<Int, Boolean>>(mutableMapOf())

    // Функція для перемикання стану розгорнутості плейлиста
    fun togglePlaylistExpansion(playlistId: Int) {
        val currentMap = expansionState.value ?: mutableMapOf()
        val currentState = currentMap[playlistId] ?: false
        currentMap[playlistId] = !currentState
        expansionState.value = currentMap // Оновлюємо стан, що викличе MediatorLiveData
    }

    // Формування змішаного списку для RecyclerView
    val playlistItems: LiveData<List<PlaylistItemType>> = MediatorLiveData<List<PlaylistItemType>>().apply {
        addSource(_allPlaylists) { playlists ->
            viewModelScope.launch(Dispatchers.IO) {
                val newState = expansionState.value ?: mutableMapOf()
                val newItems = buildMixedList(playlists, newState)
                withContext(Dispatchers.Main) { value = newItems }
            }
        }
        addSource(expansionState) { state ->
            viewModelScope.launch(Dispatchers.IO) {
                val currentPlaylists = _allPlaylists.value ?: emptyList()
                val newItems = buildMixedList(currentPlaylists, state)
                withContext(Dispatchers.Main) { value = newItems }
            }
        }
        // Можливо, додати джерело allSounds, якщо потрібно оновлювати isFavorite статус звуків
        // addSource(allSounds) { ... }
    }

    // Допоміжна функція для побудови списку
    private suspend fun buildMixedList(
        playlists: List<Playlist>,
        expansionMap: Map<Int, Boolean>
    ): List<PlaylistItemType> {
        val items = mutableListOf<PlaylistItemType>()
        playlists.forEach { playlist ->
            val isExpanded = expansionMap[playlist.id] ?: false
            items.add(PlaylistItemType.PlaylistHeader(playlist, isExpanded))
            if (isExpanded) {
                val sounds = repository.getSoundsForPlaylist(playlist.id) // Завантажуємо звуки
                sounds.forEach { sound ->
                    // TODO: Отримувати актуальний статус isFavorite з бази даних,
                    // якщо він може змінюватись поза цим ViewModel
                    items.add(PlaylistItemType.SoundItem(sound, playlist.id))
                }
            }
        }
        Log.d("ViewModelBuildList", "Built list with ${items.size} items.")
        return items
    }


    // --- Реалізація методів керування даними ---

    // Створення нового плейлиста
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            Log.d("ViewModelAction", "Creating playlist: $name")
            repository.createPlaylist(name)
            // Оновлення _allPlaylists має відбутись автоматично через Room LiveData
        }
    }

    // Видалення плейлиста
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            Log.d("ViewModelAction", "Deleting playlist: ${playlist.name}")
            repository.deletePlaylist(playlist)
            // Видаляємо стан розгорнутості для цього плейлиста
            val currentMap = expansionState.value ?: mutableMapOf()
            if (currentMap.remove(playlist.id) != null) {
                expansionState.postValue(currentMap) // Оновлюємо стан, якщо він був
            }
            // Оновлення _allPlaylists має відбутись автоматично
        }
    }

    // Видалення звуку з плейлиста
    fun removeSoundFromPlaylist(soundId: String, playlistId: Int) {
        viewModelScope.launch(Dispatchers.IO) { // Можна у фоні
            Log.d("ViewModelAction", "Removing sound $soundId from playlist $playlistId")
            repository.removeSoundFromPlaylist(soundId, playlistId)
            // Оскільки змінився вміст плейлиста (оновлено поле soundIds),
            // _allPlaylists має оновитись автоматично, що викличе перерахунок playlistItems
            Log.d("ViewModelAction", "Sound remove repository call finished.")
            // Додатково можна спробувати "пересмикнути" стан, якщо список не оновлюється
            // з якоїсь причини, але краще покладатись на реактивність LiveData
            // withContext(Dispatchers.Main) { expansionState.value = expansionState.value }
        }
    }

    // Зміна назви плейлиста
    fun updatePlaylistName(playlistId: Int, newName: String) {
        // Перевірка на порожню назву (можна додати більше валідації)
        if (newName.isBlank()) {
            Log.w("ViewModelAction", "Attempted to update playlist $playlistId with blank name.")
            return
        }
        viewModelScope.launch {
            Log.d("ViewModelAction", "Updating playlist $playlistId name to '$newName'")
            repository.updatePlaylistName(playlistId, newName)
            // Оновлення LiveData (_allPlaylists -> playlistItems) має відбутись автоматично
        }
    }

    // Зміна статусу "Улюблене" для звуку
    fun toggleFavoriteStatus(sound: Sound) {
        viewModelScope.launch {
            val newFavoriteState = !sound.isFavorite
            Log.d("ViewModelAction", "Toggling favorite for sound ${sound.name} to $newFavoriteState")
            // TODO: Потрібно додати метод в PlaylistRepository та PlaylistDao
            // для оновлення поля isFavorite в таблиці sounds
            try {
                // repository.setSoundFavoriteStatus(sound.id, newFavoriteState)
                Log.w("ViewModelAction", "Favorite status update in DB is not implemented yet!")
                // Якщо б оновлення в БД відбулось, потрібно було б оновити список.
                // Оскільки змінюється тільки звук, а не плейлист, _allPlaylists може не оновитись.
                // Потрібен був би механізм оновлення allSounds або перезапуск MediatorLiveData.
                // Поки що просто імітуємо зміну і перезапускаємо стан:
                withContext(Dispatchers.Main) { expansionState.value = expansionState.value }

            } catch (e: Exception) {
                Log.e("ViewModelAction", "Error toggling favorite status (not implemented)", e)
            }
        }
    }

    // НОВА ФУНКЦІЯ для додавання кількох звуків
    fun addSoundsToPlaylist(soundIds: List<String>, playlistId: Int) {
        if (soundIds.isEmpty()) return // Якщо список порожній, нічого не робити
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("ViewModelAction", "Adding ${soundIds.size} sounds [${soundIds.joinToString()}] to playlist $playlistId")
            repository.addSoundsToPlaylist(soundIds, playlistId) // <-- Викликаємо новий метод репозиторію
            Log.d("ViewModelAction", "Sounds add repository call finished.")
            // Оновлення списку має відбутись автоматично через зміни в БД
        }
    }
}