/*--- Це основний клас, що об'єднує таблиці (entities), DAO та конфігурацію бази даних. ---*/

package com.example.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.app.models.Playlist
import com.example.app.models.Sound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Вказую entities (таблиці), версію БД та чи експортувати схему
@Database(entities = [Sound::class, Playlist::class], version = 1, exportSchema = false)
@TypeConverters(ListConverter::class) // Вказую конвертер типів
abstract class AppDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao // Абстрактний метод для отримання DAO

    companion object {
        // @Volatile гарантує, що значення INSTANCE завжди актуальне для всіх потоків
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Метод для отримання єдиного екземпляру бази даних (Singleton)
        fun getDatabase(context: Context): AppDatabase {
            // Якщо INSTANCE не null, повернути його, інакше створити БД
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pretty_sound_database" // Назва файлу бази даних
                )
                    // .fallbackToDestructiveMigration() // Для простоти при зміні версії (видаляє старі дані). На релізі краще робити міграцію.
                    .addCallback(DatabaseCallback(context)) // Додаю callback для початкового заповнення
                    .build()
                INSTANCE = instance
                // повернути instance
                instance
            }
        }
    }

    // Callback для додавання тестових звуків при першому створенні БД
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.playlistDao())
                }
            }
        }

        suspend fun populateDatabase(playlistDao: PlaylistDao) {
            // Перевіряю, чи база вже містить звуки
            if (playlistDao.getSoundCount() == 0) {
                // Додаю тестові звуки
                playlistDao.insertSound(Sound(id = "rain_strong_1", name = "Strong Rain", filePath = "android.resource://${context.packageName}/raw/strong_rain", isFavorite = false))
                playlistDao.insertSound(Sound(id = "forest_1", name = "Forest", filePath = "android.resource://${context.packageName}/raw/forest", isFavorite = false))
                // Можна додати ще звуків за потреби

                // Можна додати початковий плейлист
                playlistDao.insertPlaylist(Playlist(name = "My First Mix", soundIds = listOf("rain_strong_1")))
            }
        }
    }
}