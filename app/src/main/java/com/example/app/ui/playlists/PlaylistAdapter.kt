package com.example.app.ui.playlists

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.models.Playlist
import com.example.app.models.Sound

class PlaylistAdapter(
    // Існуючі слухачі
    private val onPlaylistDeleteClick: (Playlist) -> Unit,
    private val onPlaylistAddSoundClick: (Playlist) -> Unit,
    private val onPlaylistFavoriteClick: (Playlist) -> Unit,
    private val onPlaylistToggleExpandClick: (Playlist) -> Unit,
    private val onPlaylistRenameClick: (Playlist) -> Unit,
    private val onSoundFavoriteClick: (Sound, Boolean) -> Unit,
    private val onSoundDeleteClick: (Sound, Int) -> Unit,
    private val isInFavoritesContext: Boolean = false

) : ListAdapter<PlaylistItemType, RecyclerView.ViewHolder>(PlaylistDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_SOUND = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlaylistItemType.PlaylistHeader -> VIEW_TYPE_HEADER
            is PlaylistItemType.SoundItem -> VIEW_TYPE_SOUND
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_playlist, parent, false)
                PlaylistHeaderViewHolder(
                    view,
                    onPlaylistDeleteClick,
                    onPlaylistAddSoundClick,
                    onPlaylistFavoriteClick,
                    onPlaylistToggleExpandClick,
                    onPlaylistRenameClick,
                    isInFavoritesContext // Передаю прапорець
                )
            }
            VIEW_TYPE_SOUND -> {
                val view = inflater.inflate(R.layout.item_playlist_sound, parent, false)
                SoundItemViewHolder(
                    view,
                    onSoundFavoriteClick,
                    onSoundDeleteClick,
                    isInFavoritesContext
                )
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlaylistItemType.PlaylistHeader -> (holder as PlaylistHeaderViewHolder).bind(item)
            is PlaylistItemType.SoundItem -> (holder as SoundItemViewHolder).bind(item.sound, item.playlistId)
        }
    }

    // ViewHolder для Заголовка Плейлиста
    class PlaylistHeaderViewHolder(
        itemView: View,
        private val onDeleteClick: (Playlist) -> Unit,
        private val onAddSoundClick: (Playlist) -> Unit,
        private val onFavoriteClick: (Playlist) -> Unit,
        private val onToggleExpandClick: (Playlist) -> Unit,
        private val onRenameClick: (Playlist) -> Unit,
        private val isInFavoritesContext: Boolean // Використовую загальне ім'я
    ) : RecyclerView.ViewHolder(itemView) {

        // View елементи
        private val nameTextView: TextView = itemView.findViewById(R.id.playlist_item_name)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.playlist_item_delete_button)
        private val addButton: ImageButton = itemView.findViewById(R.id.playlist_item_add_button)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.playlist_item_fav_button)
        private val expandArrow: ImageButton = itemView.findViewById(R.id.playlist_item_expand_arrow)
        private var currentItem: PlaylistItemType.PlaylistHeader? = null

        init {
            // слухачі для add, favorite, expand, rename
            addButton.setOnClickListener { currentItem?.playlist?.let { onAddSoundClick(it) } }
            favoriteButton.setOnClickListener { currentItem?.playlist?.let { onFavoriteClick(it) } }
            expandArrow.setOnClickListener { currentItem?.playlist?.let { onToggleExpandClick(it) } }
            itemView.setOnClickListener { currentItem?.playlist?.let { onToggleExpandClick(it) } }
            nameTextView.setOnClickListener { currentItem?.playlist?.let { onRenameClick(it) } }

            // Слухач для кнопки видалення плейлиста встановлюється, тільки якщо НЕ в улюбленому
            if (!isInFavoritesContext) {
                deleteButton.setOnClickListener { currentItem?.playlist?.let { onDeleteClick(it) } }
            }
        }

        fun bind(item: PlaylistItemType.PlaylistHeader) {
            // bind логіка для name, expand, favorite
            currentItem = item
            nameTextView.text = item.playlist.name
            expandArrow.setImageResource(
                if (item.isExpanded) android.R.drawable.arrow_up_float
                else android.R.drawable.arrow_down_float
            )
            favoriteButton.setImageResource(
                if (item.playlist.isFavorite) android.R.drawable.star_on
                else android.R.drawable.star_off
            )

            // Приховую кнопку видалення плейлиста в контексті "Улюблене"
            deleteButton.visibility = if (isInFavoritesContext) View.GONE else View.VISIBLE
        }
    }

    // ViewHolder для Елемента Звуку
    class SoundItemViewHolder(
        itemView: View,
        private val onFavoriteClick: (Sound, Boolean) -> Unit,
        private val onDeleteClick: (Sound, Int) -> Unit,
        private val isInFavoritesContext: Boolean // Використовую загальне ім'я
    ) : RecyclerView.ViewHolder(itemView) {

        // View елементи
        private val nameTextView: TextView = itemView.findViewById(R.id.sound_name)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.sound_item_favorite_button) // Ховаю в Улюбленому
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_sound)
        private val soundIcon: ImageView = itemView.findViewById(R.id.sound_icon)
        private var currentSound: Sound? = null
        private var currentPlaylistId: Int = -1
        private var isCurrentlyFavorite: Boolean = false

        init {
            // Слухач для зірочки звуку встановлюється, тільки якщо НЕ в улюбленому
            if (!isInFavoritesContext) {
                favoriteButton.setOnClickListener {
                    currentSound?.let { onFavoriteClick(it, isCurrentlyFavorite) }
                }
            }
            // Слухач для кошика звуку встановлюється, тільки якщо НЕ в улюбленому
            if (!isInFavoritesContext) {
                deleteButton.setOnClickListener {
                    currentSound?.let { onDeleteClick(it, currentPlaylistId) }
                }
            }

            itemView.setOnClickListener {
                Log.d("SoundClick", "Clicked sound: ${currentSound?.name}")
                // TODO: Викликати метод для програвання currentSound
            }
        }

        fun bind(sound: Sound, playlistId: Int) {
            // bind логіка для sound, playlistId, name, isCurrentlyFavorite
            currentSound = sound
            currentPlaylistId = playlistId
            nameTextView.text = sound.name
            isCurrentlyFavorite = sound.isFavorite

            // Налаштування іконки зірочки (якщо вона видима)
            if (!isInFavoritesContext) {
                favoriteButton.setImageResource(
                    if (isCurrentlyFavorite) android.R.drawable.star_on
                    else android.R.drawable.star_off
                )
            }

            // Приховую зірочку звуку в контексті "Улюблене"
            favoriteButton.visibility = if (isInFavoritesContext) View.GONE else View.VISIBLE

            // Приховую кошик звуку в контексті "Улюблене"
            deleteButton.visibility = if (isInFavoritesContext) View.GONE else View.VISIBLE
        }
    }

    // --- DiffUtil для змішаного списку ---
    class PlaylistDiffCallback : DiffUtil.ItemCallback<PlaylistItemType>() {
        override fun areItemsTheSame(oldItem: PlaylistItemType, newItem: PlaylistItemType): Boolean {
            return when {
                oldItem is PlaylistItemType.PlaylistHeader && newItem is PlaylistItemType.PlaylistHeader -> oldItem.id == newItem.id
                oldItem is PlaylistItemType.SoundItem && newItem is PlaylistItemType.SoundItem -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: PlaylistItemType, newItem: PlaylistItemType): Boolean {
            return oldItem == newItem
        }
    }
}