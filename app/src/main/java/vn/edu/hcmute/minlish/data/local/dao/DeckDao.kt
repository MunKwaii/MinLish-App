package vn.edu.hcmute.minlish.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vn.edu.hcmute.minlish.data.local.entity.Deck

@Dao
interface DeckDao {

    @Query("SELECT * FROM decks WHERE userId = :userId")
    fun getDecksByUser(userId: Int): Flow<List<Deck>>

    @Query("SELECT * FROM decks WHERE deckId = :deckId LIMIT 1")
    suspend fun getDeckById(deckId: Int): Deck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Update
    suspend fun updateDeck(deck: Deck)

    @Delete
    suspend fun deleteDeck(deck: Deck)
}