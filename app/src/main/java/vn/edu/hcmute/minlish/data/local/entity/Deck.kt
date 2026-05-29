package vn.edu.hcmute.minlish.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "decks",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Tính năng xịn: Xóa User thì tự động xóa luôn các Deck của người đó
        )
    ]
)
data class Deck(
    @PrimaryKey(autoGenerate = true)
    val deckId: Int = 0,
    
    val userId: Int, // Thêm cột này để kết nối với bảng User
    
    val name: String,
    val description: String,
    val tags: String
)