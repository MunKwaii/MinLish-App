package vn.edu.hcmute.minlish.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import vn.edu.hcmute.minlish.data.local.dao.DeckDao
import vn.edu.hcmute.minlish.data.local.dao.WordDao
import vn.edu.hcmute.minlish.data.local.entity.Deck
import vn.edu.hcmute.minlish.data.local.entity.Word
import vn.edu.hcmute.minlish.data.repository.VocabularyRepository

class VocabularyRepositoryImpl(
    private val deckDao: DeckDao,
    private val wordDao: WordDao
) : VocabularyRepository {

    override fun getDecksByUser(userId: Int): Flow<List<Deck>> {
        return deckDao.getDecksByUser(userId)
    }

    override fun getWordsByDeck(deckId: Int): Flow<List<Word>> {
        return wordDao.getWordsByDeck(deckId)
    }

    override suspend fun createDeck(
        userId: Int,
        name: String,
        description: String,
        tags: String
    ): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                if (name.isBlank()) {
                    return@withContext Result.failure(Exception("Tên bộ từ không được để trống"))
                }

                val deck = Deck(
                    userId = userId,
                    name = name.trim(),
                    description = description.trim(),
                    tags = tags.trim()
                )

                val id = deckDao.insertDeck(deck)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateDeck(deck: Deck): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val trimmedName = deck.name.trim()
                if (trimmedName.isBlank()) {
                    return@withContext Result.failure(Exception("Tên bộ từ không được để trống"))
                }

                val updatedDeck = deck.copy(
                    name = trimmedName,
                    description = deck.description.trim(),
                    tags = deck.tags.trim()
                )

                deckDao.updateDeck(updatedDeck)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteDeck(deck: Deck): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                deckDao.deleteDeck(deck)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun addWord(
        deckId: Int,
        word: String,
        pronunciation: String,
        meaning: String,
        description: String?,
        example: String?,
        collocations: String?,
        relatedWords: String?,
        note: String?
    ): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                if (word.isBlank()) {
                    return@withContext Result.failure(Exception("Từ vựng không được để trống"))
                }

                if (meaning.isBlank()) {
                    return@withContext Result.failure(Exception("Nghĩa của từ không được để trống"))
                }

                val wordEntity = Word(
                    deckId = deckId,
                    word = word.trim(),
                    pronunciation = pronunciation.trim(),
                    meaning = meaning.trim(),
                    description = description?.trim(),
                    example = example?.trim(),
                    collocations = collocations?.trim(),
                    relatedWords = relatedWords?.trim(),
                    note = note?.trim()
                )

                val id = wordDao.insertWord(wordEntity)
                Result.success(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateWord(word: Word): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val trimmedWord = word.word.trim()
                val trimmedMeaning = word.meaning.trim()

                if (trimmedWord.isBlank()) {
                    return@withContext Result.failure(Exception("Từ vựng không được để trống"))
                }

                if (trimmedMeaning.isBlank()) {
                    return@withContext Result.failure(Exception("Nghĩa của từ không được để trống"))
                }

                val updatedWord = word.copy(
                    word = trimmedWord,
                    pronunciation = word.pronunciation.trim(),
                    meaning = trimmedMeaning,
                    description = word.description.trimOrNull(),
                    example = word.example.trimOrNull(),
                    collocations = word.collocations.trimOrNull(),
                    relatedWords = word.relatedWords.trimOrNull(),
                    note = word.note.trimOrNull()
                )

                wordDao.updateWord(updatedWord)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteWord(word: Word): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                wordDao.deleteWord(word)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun importWords(deckId: Int, words: List<Word>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val mappedWords = words.map { word ->
                    word.copy(deckId = deckId)
                }

                wordDao.insertWords(mappedWords)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getAllWordsByUser(userId: Int): Flow<List<Word>> {
        return wordDao.getAllWordsByUser(userId)
    }

    override suspend fun getDailyStudyDeck(
        userId: Int,
        deckId: Int?,
        newWordsLimit: Int,
        currentTimestamp: Long
    ): List<Word> {
        return withContext(Dispatchers.IO) {
            val newWordsFlow = if (deckId != null && deckId != -1) {
                wordDao.getNewWordsByDeck(userId, deckId, newWordsLimit)
            } else {
                wordDao.getAllNewWordsByUser(userId, newWordsLimit)
            }

            val dueWordsFlow = if (deckId != null && deckId != -1) {
                wordDao.getWordsDueForReviewByDeck(userId, deckId, currentTimestamp)
            } else {
                wordDao.getAllWordsDueForReviewByUser(userId, currentTimestamp)
            }

            val newWords = newWordsFlow.first()
            val dueWords = dueWordsFlow.first()

            dueWords + newWords
        }
    }

    private fun String?.trimOrNull(): String? {
        return this?.trim()?.ifBlank { null }
    }
}
