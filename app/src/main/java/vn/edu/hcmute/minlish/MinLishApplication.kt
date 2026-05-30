package vn.edu.hcmute.minlish

import android.app.Application
import vn.edu.hcmute.minlish.data.local.MinlishDatabase
import vn.edu.hcmute.minlish.data.repository.UserRepository
import vn.edu.hcmute.minlish.data.repository.impl.UserRepositoryImpl
import vn.edu.hcmute.minlish.data.util.SessionManager

class MinLishApplication : Application() {
    val database: MinlishDatabase by lazy { MinlishDatabase.getDatabase(this) }
    val userRepository: UserRepository by lazy { UserRepositoryImpl(database.userDao()) }
    val sessionManager: SessionManager by lazy { SessionManager(this) }
}
