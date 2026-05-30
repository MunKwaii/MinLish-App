package vn.edu.hcmute.minlish.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.edu.hcmute.minlish.data.local.dao.UserDao
import vn.edu.hcmute.minlish.data.local.entity.User
import vn.edu.hcmute.minlish.data.repository.UserRepository

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserByEmail(email)
            return@withContext user
        }
    }

    override suspend fun registerUser(user: User): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = userDao.getUserByEmail(user.email)
                if (existing != null) {
                    return@withContext Result.failure(Exception("Email đã tồn tại trong hệ thống!"))
                } else {
                    val hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(user.passwordHash, org.mindrot.jbcrypt.BCrypt.gensalt())
                    val securedUser = user.copy(passwordHash = hashedPassword)
                    val id = userDao.insertUser(securedUser)
                    return@withContext Result.success(id)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    override suspend fun loginWithGoogle(email: String, name: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = userDao.getUserByEmail(email)
                if (existing != null) {
                    return@withContext Result.success(existing)
                } else {
                    val newUser = User(
                        email = email,
                        name = name,
                        passwordHash = "" // Không dùng mật khẩu khi đăng nhập bằng Google
                    )
                    val id = userDao.insertUser(newUser)
                    val registeredUser = newUser.copy(userId = id.toInt())
                    return@withContext Result.success(registeredUser)
                }
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }
}
