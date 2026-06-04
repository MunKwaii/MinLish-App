package vn.edu.hcmute.minlish.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vn.edu.hcmute.minlish.data.local.entity.Word

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE deckId = :deckId")
    fun getWordsByDeck(deckId: Int): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE wordId = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Int): Word?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Delete
    suspend fun deleteWord(word: Word)

    @Query("DELETE FROM words WHERE deckId = :deckId")
    suspend fun deleteWordsByDeck(deckId: Int)
}