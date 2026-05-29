package vn.edu.hcmute.minlish.data.repository

import vn.edu.hcmute.minlish.data.local.entity.User

interface UserRepository {
    suspend fun getUserByEmail(email: String): User?
    suspend fun registerUser(user: User): Result<Long>
    suspend fun loginWithGoogle(email: String, name: String): Result<User>
}
