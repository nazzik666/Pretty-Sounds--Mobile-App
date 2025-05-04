package com.example.app.ui.playlists

import com.example.app.models.Playlist
import com.example.app.models.Sound

// Запечатаний клас (sealed class) для представлення елементів у спільному списку
sealed class PlaylistItemType {
    // Додаю поле isExpanded зі значенням за замовчуванням false
    data class PlaylistHeader(
        val playlist: Playlist,
        var isExpanded: Boolean = false // <-- ДОДАНО
    ) : PlaylistItemType() {
        val id: String = "header_${playlist.id}"
    }

    data class SoundItem(
        val sound: Sound,
        val playlistId: Int
    ) : PlaylistItemType() {
        val id: String = "sound_${playlistId}_${sound.id}"
    }
}