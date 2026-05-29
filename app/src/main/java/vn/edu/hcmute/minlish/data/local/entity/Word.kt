package vn.edu.hcmute.minlish.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["deckId"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Word(
    @PrimaryKey(autoGenerate = true)
    val wordId: Int = 0,
    val deckId: Int,

    val word: String,
    val pronunciation: String,
    val meaning: String,
    val description: String?,
    val example: String?,
    val collocations: String?,
    val relatedWords: String?,
    val note: String?,

    // Thuật toán Spaced Repetition (SRS)
    val nextReviewTime: Long = 0L,
    val easeFactor: Float = 2.5f,
    val interval: Int = 0
)