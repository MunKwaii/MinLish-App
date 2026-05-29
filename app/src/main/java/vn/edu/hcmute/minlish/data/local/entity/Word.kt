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
    val description: String? = null,
    val example: String? = null,
    val collocations: String? = null,
    val relatedWords: String? = null,
    val note: String? = null
)