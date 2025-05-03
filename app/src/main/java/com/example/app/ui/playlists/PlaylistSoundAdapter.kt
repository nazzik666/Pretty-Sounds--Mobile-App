package com.example.app.ui.playlists // Пакет той самий

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView // Імпорт для ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.models.Sound // Тепер працюємо з моделлю Sound

// Адаптер для списку ЗВУКІВ всередині плейлиста
class PlaylistSoundAdapter( // Назва класу відповідає назві файлу
    private val onSoundClicked: (Sound) -> Unit,
    private val onDeleteClicked: (Sound) -> Unit
) : ListAdapter<Sound, PlaylistSoundAdapter.SoundViewHolder>(SoundDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_sound, parent, false) // Використовуємо макет для ЗВУКУ
        return SoundViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        holder.bind(getItem(position), onSoundClicked, onDeleteClicked)
    }

    // ViewHolder для одного ЗВУКУ
    class SoundViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val soundNameTextView: TextView = itemView.findViewById(R.id.sound_name)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_sound)
        private val soundIcon: ImageView = itemView.findViewById(R.id.sound_icon) // Іконка звуку

        fun bind(
            sound: Sound, // Приймає об'єкт Sound
            onSoundClicked: (Sound) -> Unit,
            onDeleteClicked: (Sound) -> Unit
        ) {
            soundNameTextView.text = sound.name
            // Можна встановити іконку, якщо вона є
            // soundIcon.setImageResource(...)

            itemView.setOnClickListener { onSoundClicked(sound) }
            deleteButton.setOnClickListener { onDeleteClicked(sound) }
        }
    }

    // DiffUtil для ЗВУКІВ
    class SoundDiffCallback : DiffUtil.ItemCallback<Sound>() {
        override fun areItemsTheSame(oldItem: Sound, newItem: Sound): Boolean {
            return oldItem.id == newItem.id // Порівнюємо ID звуків
        }

        override fun areContentsTheSame(oldItem: Sound, newItem: Sound): Boolean {
            return oldItem == newItem // Порівнюємо об'єкти Sound
        }
    }
}