package vn.edu.hcmute.minlish.data.repository

import kotlinx.coroutines.flow.Flow
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.Word

interface VocabularyRepository {

    fun getDecksByUser(userId: Int): Flow<List<Deck>>

    fun getWordsByDeck(deckId: Int): Flow<List<Word>>

    suspend fun createDeck(
        userId: Int,
        name: String,
        description: String,
        tags: String
    ): Result<Long>

    suspend fun addWord(
        deckId: Int,
        word: String,
        pronunciation: String,
        meaning: String,
        description: String?,
        example: String?,
        collocations: String?,
        relatedWords: String?,
        note: String?
    ): Result<Long>

    suspend fun importWords(deckId: Int, words: List<Word>): Result<Unit>
}