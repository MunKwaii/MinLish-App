package vn.edu.hcmute.minlish.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,

    val email: String,
    val name: String,
    val passwordHash: String,
    val learningGoal: String = "",
    val level: String = "",
    val userType: String = "",

    val totalWordsLearned: Int = 0,
    val currentStreak: Int = 0,
    val accuracyRate: Float = 0f
)