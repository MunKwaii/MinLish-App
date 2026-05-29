package vn.edu.hcmute.minlish.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcard_progress",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Word::class,
            parentColumns = ["wordId"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FlashcardProgress(
    @PrimaryKey(autoGenerate = true)
    val progressId: Int = 0,
    val userId: Int,
    val wordId: Int,
    
    // Lưu các thông số của thuật toán Anki/Spaced Repetition
    val easeFactor: Float = 2.5f,
    val interval: Int = 0,
    val nextReviewTime: Long = 0L 
)