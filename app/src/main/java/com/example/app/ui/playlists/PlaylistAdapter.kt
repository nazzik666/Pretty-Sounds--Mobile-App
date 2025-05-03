package com.example.app.ui.playlists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.models.Playlist

// Адаптер для RecyclerView в MainActivity
class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit, // Лямбда для кліку на елемент (відкрити)
    private val onDeleteClick: (Playlist) -> Unit   // Лямбда для кліку на видалення
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    // Створення ViewHolder (тримача) для елемента списку
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false) // Використовую наш item_playlist.xml
        return PlaylistViewHolder(itemView, onPlaylistClick, onDeleteClick) // Передаю у ViewHolder
    }

    // Заповнення ViewHolder даними
    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val currentPlaylist = getItem(position)
        holder.bind(currentPlaylist)
    }

    // ViewHolder - зберігає посилання на View всередині одного рядка списку
    class PlaylistViewHolder(
        itemView: View,
        private val onPlaylistClick: (Playlist) -> Unit, // Зберігаю лямбди
        private val onDeleteClick: (Playlist) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        // Знахожу View за ID всередині item_playlist.xml
        private val nameTextView: TextView = itemView.findViewById(R.id.playlist_item_name)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.playlist_item_delete_button)
        private val openButton: ImageButton = itemView.findViewById(R.id.playlist_item_open_button)
        private var currentPlaylist: Playlist? = null // Зберігаю поточний плейлист для обробки кліків

        init {
            // Встановлюю слухачі кліків один раз при створенні ViewHolder
            // Клік на весь рядок АБО на кнопку "відкрити"
            itemView.setOnClickListener {
                currentPlaylist?.let { onPlaylistClick(it) }
            }
            openButton.setOnClickListener {
                currentPlaylist?.let { onPlaylistClick(it) }
            }
            // Клік на кнопку "видалити"
            deleteButton.setOnClickListener {
                currentPlaylist?.let { onDeleteClick(it) }
            }
        }

        // Метод для заповнення View даними конкретного плейлиста
        fun bind(playlist: Playlist) {
            currentPlaylist = playlist // Зберігаємо плейлист
            nameTextView.text = playlist.name
            // Тут можна ще встановлювати іконки тощо, якщо потрібно
        }
    }

    // DiffUtil для ефективного оновлення списку
    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}