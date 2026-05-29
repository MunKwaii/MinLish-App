package vn.edu.hcmute.minlish.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.edu.hcmute.minlish.data.local.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: User): Long
}
