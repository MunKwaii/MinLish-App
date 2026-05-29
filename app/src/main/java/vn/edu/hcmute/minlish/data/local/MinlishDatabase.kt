package vn.edu.hcmute.minlish.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vn.edu.hcmute.minlish.data.local.dao.UserDao
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
    version = 1,
    exportSchema = false
)
abstract class MinlishDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: MinlishDatabase? = null

        fun getDatabase(context: Context): MinlishDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MinlishDatabase::class.java,
                    "minlish_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
