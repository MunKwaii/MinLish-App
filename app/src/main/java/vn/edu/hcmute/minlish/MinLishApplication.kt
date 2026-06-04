package vn.edu.hcmute.minlish

import android.app.Application
import vn.edu.hcmute.minlish.data.local.MinlishDatabase
import vn.edu.hcmute.minlish.data.repository.ProgressRepository
import vn.edu.hcmute.minlish.data.repository.UserRepository
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository
import vn.edu.hcmute.minlish.data.repository.impl.ProgressRepositoryImpl
import vn.edu.hcmute.minlish.data.repository.impl.UserRepositoryImpl
import vn.edu.hcmute.minlish.data.repository.impl.VocabularyRepositoryImpl
import vn.edu.hcmute.minlish.data.util.SessionManager

class MinLishApplication : Application() {

    val database: MinlishDatabase by lazy {
        MinlishDatabase.getDatabase(this)
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao())
    }

    val vocabularyRepository: VocabularyRepository by lazy {
        VocabularyRepositoryImpl(
            deckDao = database.deckDao(),
            wordDao = database.wordDao()
        )
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(database.studyProgressDao())
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(this)
    }

    val settingsManager: vn.edu.hcmute.minlish.data.util.SettingsManager by lazy {
        vn.edu.hcmute.minlish.data.util.SettingsManager(this)
    }
}