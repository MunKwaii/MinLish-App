package vn.edu.hcmute.minlish.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true)
    val deckId: Int = 0,

    val name: String,
    val description: String,
    val tags: String
)