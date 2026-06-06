package vn.edu.hcmute.minlish.data.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.edu.hcmute.minlish.data.local.dao.UserDao
import vn.edu.hcmute.minlish.data.local.entity.User
import vn.edu.hcmute.minlish.data.repository.impl.UserRepositoryImpl

class UserRepositoryImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var fakeUserDao: FakeUserDao

    @Before
    fun setup() {
        fakeUserDao = FakeUserDao()
        userRepository = UserRepositoryImpl(fakeUserDao)
    }

    @Test
    fun `registerUser returns failure when email already exists`() = runBlocking {
        // GIVEN
        val existingUser = User(
            userId = 1,
            email = "test@example.com",
            name = "Test User",
            passwordHash = "hash123"
        )
        fakeUserDao.insertUser(existingUser)

        val newUser = User(
            email = "test@example.com", // Same email
            name = "New User",
            passwordHash = "newhash"
        )

        // WHEN
        val result = userRepository.registerUser(newUser)

        // THEN
        assertTrue(result.isFailure)
        assertEquals("Email đã tồn tại trong hệ thống!", result.exceptionOrNull()?.message)
    }

    @Test
    fun `registerUser success when email is new and hashes password`() = runBlocking {
        // GIVEN
        val newUser = User(
            email = "new@example.com",
            name = "New User",
            passwordHash = "plainpassword"
        )

        // WHEN
        val result = userRepository.registerUser(newUser)

        // THEN
        assertTrue(result.isSuccess)
        val insertedId = result.getOrNull()
        assertTrue(insertedId != null && insertedId > 0)
        
        val insertedUser = fakeUserDao.getUserByEmail("new@example.com")
        assertTrue(insertedUser != null)
        // Ensure the password was hashed by BCrypt (starts with $2a$)
        assertTrue(insertedUser!!.passwordHash.startsWith("$2a$"))
    }
}

// Giả lập UserDao để test độc lập không cần database thực
class FakeUserDao : UserDao {
    private val users = mutableListOf<User>()
    private var nextId = 1

    override fun getUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }

    override fun insertUser(user: User): Long {
        if (users.any { it.email == user.email }) {
            throw Exception("UNIQUE constraint failed: users.email")
        }
        val userWithId = user.copy(userId = nextId++)
        users.add(userWithId)
        return userWithId.userId.toLong()
    }

    override fun updateUser(user: User) {
        val index = users.indexOfFirst { it.userId == user.userId }
        if (index != -1) {
            users[index] = user
        }
    }
}
