package vn.edu.hcmute.minlish.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vn.edu.hcmute.minlish.data.local.dao.DeckDao
import vn.edu.hcmute.minlish.data.local.dao.UserDao
import vn.edu.hcmute.minlish.data.local.dao.WordDao
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.FlashcardProgress
import vn.edu.hcmute.minlish.data.local.entity.StudyProgress
import vn.edu.hcmute.minlish.data.local.entity.User
import vn.edu.hcmute.minlish.data.local.entity.Word

@Database(
    entities = [
        User::class,
        Deck::class,
        Word::class,
        FlashcardProgress::class,
        StudyProgress::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MinlishDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deckDao(): DeckDao
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: MinlishDatabase? = null

        fun getDatabase(context: Context): MinlishDatabase {
            val currentInstance = INSTANCE
            if (currentInstance != null) {
                return currentInstance
            }
            synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    MinlishDatabase::class.java,
                    "minlish_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = db
                return db
            }
        }
    }
}
