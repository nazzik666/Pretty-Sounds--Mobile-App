package com.example.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.app.data.ListConverter

@Entity(tableName = "playlists") // Таблиця для плейлистів
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID плейлиста генерується автоматично
    val name: String,              // Назва плейлиста
    val soundIds: List<String>     // Список ID звуків, що входять до плейлиста
)