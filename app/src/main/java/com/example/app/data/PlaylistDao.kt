package com.example.app.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.app.models.Playlist
import com.example.app.models.Sound

@Dao
interface PlaylistDao {

    // --- Операції з плейлистами ---

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Якщо плейлист існує, замінити його
    suspend fun insertPlaylist(playlist: Playlist) // suspend вказує, що функція має викликатись з корутини

    @Update
    suspend fun updatePlaylist(playlist: Playlist) // Оновити існуючий плейлист

    @Delete
    suspend fun deletePlaylist(playlist: Playlist) // Видалити плейлист

    @Query("SELECT * FROM playlists ORDER BY name ASC") // Отримати всі плейлисти, відсортовані за назвою
    fun getAllPlaylists(): LiveData<List<Playlist>> // LiveData автоматично оновлює UI при зміні даних

    @Query("SELECT * FROM playlists WHERE id = :playlistId") // Отримати плейлист за його ID
    suspend fun getPlaylistById(playlistId: Int): Playlist?

    // --- Операції зі звуками (потрібні для плейлистів) ---

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Додати звук, якщо його ще немає
    suspend fun insertSound(sound: Sound)

    // Функція для отримання конкретних звуків за списком їх ID
    // Room автоматично виконає запит для кожного ID зі списку
    @Query("SELECT * FROM sounds WHERE id IN (:soundIds)")
    suspend fun getSoundsByIds(soundIds: List<String>): List<Sound>

    // Додаю функцію для отримання всіх звуків (для вікна додавання до плейлиста)
    @Query("SELECT * FROM sounds ORDER BY name ASC")
    fun getAllSounds(): LiveData<List<Sound>> // LiveData для списку всіх звуків

    // --- Тестові дані (опціонально, для початкового заповнення) ---
    @Query("SELECT COUNT(*) FROM sounds") // Перевірити, чи є звуки в базі
    suspend fun getSoundCount(): Int
}