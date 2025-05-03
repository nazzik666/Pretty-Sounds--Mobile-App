package com.example.app.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.data.PlaylistRepository

// Фабрика для створення ViewModel з залежностями
class ViewModelFactory(private val repository: PlaylistRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Перевіряю, який ViewModel потрібно створити
        if (modelClass.isAssignableFrom(PlaylistsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistsViewModel(repository) as T // Створюю PlaylistsViewModel
        }
        if (modelClass.isAssignableFrom(PlaylistDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistDetailsViewModel(repository) as T // Створюю PlaylistDetailsViewModel
        }
        // Можна додати інші ViewModel тут за потреби

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}