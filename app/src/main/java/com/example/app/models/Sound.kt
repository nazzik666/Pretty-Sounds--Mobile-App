package com.example.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sounds") // Таблиця для звуків
data class Sound(
    @PrimaryKey val id: String,      // Унікальний ID (наприклад, "rain_strong", "forest_birds")
    val name: String,              // Назва для відображення ("Сильний дощ", "Ліс з птахами")
    val filePath: String,          // Шлях до файлу (реальний або placeholder)
    var isFavorite: Boolean = false // Для функціоналу "Улюблене"
)