package vn.edu.hcmute.minlish.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_progress",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudyProgress(
    @PrimaryKey(autoGenerate = true)
    val progressId: Int = 0,
    
    val userId: Int, // Thêm cột này để biết tiến độ này của ai
    
    val date: String,
    val newWordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0
)