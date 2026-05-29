package vn.edu.hcmute.minlish.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.edu.hcmute.minlish.data.local.dao.UserDao
import vn.edu.hcmute.minlish.data.local.entity.User

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {
    override suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    override suspend fun registerUser(user: User): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val existing = userDao.getUserByEmail(user.email)
            if (existing != null) {
                Result.failure(Exception("Email đã tồn tại trong hệ thống!"))
            } else {
                val id = userDao.insertUser(user)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
