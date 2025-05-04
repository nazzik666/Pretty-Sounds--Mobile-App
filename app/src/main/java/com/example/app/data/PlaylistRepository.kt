package com.example.app.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.app.models.Playlist
import com.example.app.models.Sound

// Репозиторій приймає DAO як залежність
class PlaylistRepository(private val playlistDao: PlaylistDao) {

    // Отримую всі плейлисти як LiveData (просто передаю з DAO)
    val allPlaylists: LiveData<List<Playlist>> = playlistDao.getAllPlaylists()

    // Отримую всі звуки як LiveData
    val allSounds: LiveData<List<Sound>> = playlistDao.getAllSounds()

    // LiveData для списку Улюбленого
    val favoritePlaylists: LiveData<List<Playlist>> = playlistDao.getFavoritePlaylists()
    val favoriteSounds: LiveData<List<Sound>> = playlistDao.getFavoriteSounds()

    // Функція для створення нового плейлиста
    suspend fun createPlaylist(name: String) {
        val newPlaylist = Playlist(name = name, soundIds = emptyList()) // Створюю плейлист з порожнім списком звуків
        playlistDao.insertPlaylist(newPlaylist)
    }

    // Функція для видалення плейлиста
    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    // Функція для отримання плейлиста за ID
    suspend fun getPlaylistById(playlistId: Int): Playlist? {
        return playlistDao.getPlaylistById(playlistId)
    }

    // Функція для редагування назви плейлиста
    suspend fun updatePlaylistName(playlistId: Int, newName: String) { // <-- НОВИЙ МЕТОД
        playlistDao.updatePlaylistName(playlistId, newName)
    }

    // Функція для отримання звуків за списком ID
    suspend fun getSoundsForPlaylist(soundIds: List<String>): List<Sound> {
        if (soundIds.isEmpty()) {
            return emptyList() // Повертаємо порожній список, якщо ID немає
        }
        return playlistDao.getSoundsByIds(soundIds)
    }

    // Функція для додавання звуку до плейлиста
    suspend fun addSoundsToPlaylist(soundIdsToAdd: List<String>, playlistId: Int) {
        if (soundIdsToAdd.isEmpty()) return // Якщо нічого додавати, виходимо

        Log.d("RepoAddSounds", "Repo: Adding sounds $soundIdsToAdd to playlist $playlistId")
        try {
            val playlist = playlistDao.getPlaylistById(playlistId) // Читаємо плейлист ОДИН РАЗ
            if (playlist != null) {
                val currentSoundIds = playlist.soundIds.toMutableList()
                var changed = false
                // Додаємо всі нові ID, яких ще немає
                soundIdsToAdd.forEach { soundId ->
                    if (!currentSoundIds.contains(soundId)) {
                        currentSoundIds.add(soundId)
                        changed = true
                    }
                }

                // Якщо список реально змінився, оновлюємо БД ОДИН РАЗ
                if (changed) {
                    val updatedPlaylist = playlist.copy(soundIds = currentSoundIds.toList()) // Копіюємо з оновленим списком
                    Log.d("RepoAddSounds", "Repo: Updating playlist ${playlist.id} in DB. New sounds: ${updatedPlaylist.soundIds}")
                    playlistDao.updatePlaylist(updatedPlaylist) // Записуємо ОДИН РАЗ
                } else {
                    Log.d("RepoAddSounds", "Repo: No new sounds were added to playlist ${playlist.id}")
                }
            } else {
                Log.w("RepoAddSounds", "Repo: Playlist with ID $playlistId not found for adding sounds.")
            }
        } catch (e: Exception) {
            Log.e("RepoAddSounds", "Repo: Error adding multiple sounds", e)
        }
    }

    // Функція для видалення звуку з плейлиста
    suspend fun removeSoundFromPlaylist(soundId: String, playlistId: Int) {
        Log.d("SoundDelete", "Repo: Attempting to remove sound $soundId from playlist $playlistId")
        try { // Додамо try-catch для безпеки
            val playlist = playlistDao.getPlaylistById(playlistId)
            if (playlist != null) {
                Log.d("SoundDelete", "Repo: Found playlist '${playlist.name}' with sounds: ${playlist.soundIds}")
                val updatedSoundIds = playlist.soundIds.toMutableList()
                val removed = updatedSoundIds.remove(soundId) // Спробуємо видалити

                if (removed) {
                    Log.d("SoundDelete", "Repo: Sound ID $soundId removed. New list: $updatedSoundIds")
                    val updatedPlaylist = playlist.copy(soundIds = updatedSoundIds)
                    playlistDao.updatePlaylist(updatedPlaylist) // Оновлюємо в базі
                    Log.d("SoundDelete", "Repo: Playlist updated in DB.")
                } else {
                    Log.w("SoundDelete", "Repo: Sound ID $soundId was NOT found in playlist ${playlist.id}")
                }
            } else {
                Log.w("SoundDelete", "Repo: Playlist with ID $playlistId not found.")
            }
        } catch (e: Exception) {
            Log.e("SoundDelete", "Repo: Error removing sound", e)
        }
    }

    // suspend функції для зміни статусу Улюбленого
    suspend fun setSoundFavoriteStatus(soundId: String, isFavorite: Boolean) {
        playlistDao.setSoundFavoriteStatus(soundId, isFavorite)
    }
    suspend fun setPlaylistFavoriteStatus(playlistId: Int, isFavorite: Boolean) {
        playlistDao.setPlaylistFavoriteStatus(playlistId, isFavorite)
    }

    // Метод для отримання звуків для конкретного плейлиста
    suspend fun getSoundsForPlaylist(playlistId: Int): List<Sound> {
        val playlist = playlistDao.getPlaylistById(playlistId) // Потрібен getPlaylistById в DAO
        if (playlist != null && playlist.soundIds.isNotEmpty()) {
            return playlistDao.getSoundsByIdsSuspend(playlist.soundIds) // Використовую нову suspend функцію
        }
        return emptyList()
    }
}