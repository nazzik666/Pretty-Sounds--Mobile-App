package com.example.app.ui.playlists

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView // Переконайтесь, що імпортовано ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.models.Playlist
import com.example.app.models.Sound

// Адаптер для змішаного списку (Заголовки Плейлистів + Звуки)
class PlaylistAdapter(
    // Оновлений набір слухачів
    private val onPlaylistDeleteClick: (Playlist) -> Unit,
    private val onPlaylistAddSoundClick: (Playlist) -> Unit,
    private val onPlaylistFavoriteClick: (Playlist) -> Unit,
    private val onPlaylistToggleExpandClick: (Playlist) -> Unit,
    private val onPlaylistRenameClick: (Playlist) -> Unit,
    private val onSoundFavoriteClick: (Sound, Boolean) -> Unit,
    private val onSoundDeleteClick: (Sound, Int) -> Unit
) : ListAdapter<PlaylistItemType, RecyclerView.ViewHolder>(PlaylistDiffCallback()) { // Тип даних PlaylistItemType

    // Константи для типів View
    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_SOUND = 2
    }

    // Визначаємо тип елемента за позицією
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PlaylistItemType.PlaylistHeader -> VIEW_TYPE_HEADER
            is PlaylistItemType.SoundItem -> VIEW_TYPE_SOUND
            // Можна додати обробку null або невідомих типів, якщо потрібно
            // null -> throw IllegalStateException("Item at position $position is null")
            // else -> throw IllegalArgumentException("Unknown view type at position $position")
        }
    }

    // Створюємо відповідний ViewHolder в залежності від типу View
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_playlist, parent, false) // Макет заголовка
                PlaylistHeaderViewHolder(
                    view,
                    onPlaylistDeleteClick,
                    onPlaylistAddSoundClick,
                    onPlaylistFavoriteClick,
                    onPlaylistToggleExpandClick,
                    onPlaylistRenameClick
                )
            }
            VIEW_TYPE_SOUND -> {
                val view = inflater.inflate(R.layout.item_playlist_sound, parent, false) // Макет звуку
                SoundItemViewHolder(view, onSoundFavoriteClick, onSoundDeleteClick)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    // Заповнюємо ViewHolder даними
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PlaylistItemType.PlaylistHeader -> (holder as PlaylistHeaderViewHolder).bind(item)
            is PlaylistItemType.SoundItem -> (holder as SoundItemViewHolder).bind(item.sound, item.playlistId)
            // else -> Log.w("AdapterBind", "Unknown item type at position $position") // Обробка інших випадків (наприклад, null)
        }
    }

    // --- ViewHolder для Заголовка Плейлиста ---
    class PlaylistHeaderViewHolder(
        itemView: View,
        // Оновлені слухачі
        private val onDeleteClick: (Playlist) -> Unit,
        private val onAddSoundClick: (Playlist) -> Unit,
        private val onFavoriteClick: (Playlist) -> Unit,
        private val onToggleExpandClick: (Playlist) -> Unit,
        private val onRenameClick: (Playlist) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        // Знаходимо View
        private val nameTextView: TextView = itemView.findViewById(R.id.playlist_item_name)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.playlist_item_delete_button)
        private val addButton: ImageButton = itemView.findViewById(R.id.playlist_item_add_button)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.playlist_item_fav_button)
        private val expandArrow: ImageButton = itemView.findViewById(R.id.playlist_item_expand_arrow)
        private var currentItem: PlaylistItemType.PlaylistHeader? = null

        init {
            // Встановлюємо слухачі кліків
            deleteButton.setOnClickListener { currentItem?.playlist?.let { onDeleteClick(it) } }
            addButton.setOnClickListener { currentItem?.playlist?.let { onAddSoundClick(it) } }
            favoriteButton.setOnClickListener { currentItem?.playlist?.let { onFavoriteClick(it) } }
            expandArrow.setOnClickListener { currentItem?.playlist?.let { onToggleExpandClick(it) } }
            // Клік на весь рядок розгортає/згортає
            itemView.setOnClickListener { currentItem?.playlist?.let { onToggleExpandClick(it) } }

            nameTextView.setOnClickListener {
                currentItem?.playlist?.let { onRenameClick(it) } // Викликаємо новий слухач
            }
        }

        // Заповнюємо View даними
        fun bind(item: PlaylistItemType.PlaylistHeader) {
            currentItem = item
            nameTextView.text = item.playlist.name
            // Оновлюємо іконку стрілки
            expandArrow.setImageResource(
                if (item.isExpanded) android.R.drawable.arrow_up_float // Іконка "вгору"
                else android.R.drawable.arrow_down_float // Іконка "вниз"
            )
            // TODO: Оновлення стану кнопки "Улюблене" для плейлиста
            favoriteButton.setImageResource(android.R.drawable.star_off) // Поки що завжди "вимкнено"
        }
    }

    // --- ViewHolder для Елемента Звуку ---
    class SoundItemViewHolder(
        itemView: View,
        private val onFavoriteClick: (Sound, Boolean) -> Unit,
        private val onDeleteClick: (Sound, Int) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        // Знаходимо View
        private val nameTextView: TextView = itemView.findViewById(R.id.sound_name)
        private val favoriteButton: ImageButton = itemView.findViewById(R.id.sound_item_favorite_button)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_sound)
        private val soundIcon: ImageView = itemView.findViewById(R.id.sound_icon)
        private var currentSound: Sound? = null
        private var currentPlaylistId: Int = -1
        private var isCurrentlyFavorite: Boolean = false // Зберігаємо стан для передачі

        init {
            // Встановлюємо слухачі кліків
            favoriteButton.setOnClickListener {
                currentSound?.let { onFavoriteClick(it, isCurrentlyFavorite) }
            }
            deleteButton.setOnClickListener {
                currentSound?.let { onDeleteClick(it, currentPlaylistId) }
            }
            itemView.setOnClickListener {
                // Можна додати дію при кліку на сам звук (наприклад, програвання)
                Log.d("SoundClick", "Clicked sound: ${currentSound?.name}")
                // TODO: Викликати метод для програвання currentSound
            }
        }

        // Заповнюємо View даними
        fun bind(sound: Sound, playlistId: Int) {
            currentSound = sound
            currentPlaylistId = playlistId
            nameTextView.text = sound.name
            isCurrentlyFavorite = sound.isFavorite // Отримуємо актуальний стан

            // Встановлюємо іконку "Улюблене"
            favoriteButton.setImageResource(
                if (isCurrentlyFavorite) android.R.drawable.star_on // Іконка заповненої зірки
                else android.R.drawable.star_off // Іконка порожньої зірки
            )
            // Можна встановити іконку для типу звуку
            // soundIcon.setImageResource(...)
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
            return oldItem == newItem // Порівнюємо data класи
        }
    }
}