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

    @Query("SELECT w.* FROM words w INNER JOIN decks d ON w.deckId = d.deckId WHERE d.userId = :userId")
    fun getAllWordsByUser(userId: Int): Flow<List<Word>>

    // Lấy các từ mới chưa học trong một bộ từ cụ thể
    @Query("SELECT * FROM words WHERE deckId = :deckId AND wordId NOT IN (SELECT wordId FROM flashcard_progress WHERE userId = :userId) LIMIT :limit")
    fun getNewWordsByDeck(userId: Int, deckId: Int, limit: Int): Flow<List<Word>>

    // Lấy các từ mới chưa học trên toàn bộ ứng dụng của user
    @Query("SELECT w.* FROM words w INNER JOIN decks d ON w.deckId = d.deckId WHERE d.userId = :userId AND w.wordId NOT IN (SELECT wordId FROM flashcard_progress WHERE userId = :userId) LIMIT :limit")
    fun getAllNewWordsByUser(userId: Int, limit: Int): Flow<List<Word>>

    // Lấy các từ đã đến hạn ôn tập trong một bộ từ cụ thể
    @Query("SELECT w.* FROM words w INNER JOIN flashcard_progress p ON w.wordId = p.wordId WHERE w.deckId = :deckId AND p.userId = :userId AND p.nextReviewTime <= :currentTimestamp")
    fun getWordsDueForReviewByDeck(userId: Int, deckId: Int, currentTimestamp: Long): Flow<List<Word>>

    // Lấy các từ đã đến hạn ôn tập trên toàn bộ ứng dụng của user
    @Query("SELECT w.* FROM words w INNER JOIN decks d ON w.deckId = d.deckId INNER JOIN flashcard_progress p ON w.wordId = p.wordId WHERE d.userId = :userId AND p.userId = :userId AND p.nextReviewTime <= :currentTimestamp")
    fun getAllWordsDueForReviewByUser(userId: Int, currentTimestamp: Long): Flow<List<Word>>
}