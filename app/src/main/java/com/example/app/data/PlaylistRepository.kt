package com.example.app.data

import androidx.lifecycle.LiveData
import com.example.app.models.Playlist
import com.example.app.models.Sound

// Репозиторій приймає DAO як залежність
class PlaylistRepository(private val playlistDao: PlaylistDao) {

    // Отримую всі плейлисти як LiveData (просто передаю з DAO)
    val allPlaylists: LiveData<List<Playlist>> = playlistDao.getAllPlaylists()

    // Отримую всі звуки як LiveData
    val allSounds: LiveData<List<Sound>> = playlistDao.getAllSounds()

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

    // Функція для отримання звуків за списком ID
    suspend fun getSoundsForPlaylist(soundIds: List<String>): List<Sound> {
        if (soundIds.isEmpty()) {
            return emptyList() // Повертаємо порожній список, якщо ID немає
        }
        return playlistDao.getSoundsByIds(soundIds)
    }

    // Функція для додавання звуку до плейлиста
    suspend fun addSoundToPlaylist(soundId: String, playlistId: Int) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let { // Якщо плейлист існує
            // Створюю змінний список ID зі старого списку
            val updatedSoundIds = it.soundIds.toMutableList()
            // Додаю новий ID, тільки якщо його ще немає
            if (!updatedSoundIds.contains(soundId)) {
                updatedSoundIds.add(soundId)
                // Створюю оновлений об'єкт плейлиста
                val updatedPlaylist = it.copy(soundIds = updatedSoundIds)
                // Оновлюю плейлист у базі даних
                playlistDao.updatePlaylist(updatedPlaylist)
            }
        }
    }

    // Функція для видалення звуку з плейлиста
    suspend fun removeSoundFromPlaylist(soundId: String, playlistId: Int) {
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let {
            val updatedSoundIds = it.soundIds.toMutableList()
            // Видаляю ID зі списку
            if (updatedSoundIds.remove(soundId)) {
                // Якщо видалення було успішним, оновлюю плейлист
                val updatedPlaylist = it.copy(soundIds = updatedSoundIds)
                playlistDao.updatePlaylist(updatedPlaylist)
            }
        }
    }
}