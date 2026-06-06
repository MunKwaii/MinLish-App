package vn.edu.hcmute.minlish.data.remote.dictionary

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class DictionaryApiDataSource {

    suspend fun lookupWord(
        word: String,
        lang: String = "en",
        defLang: String = "vi"
    ): DictionaryLookupResult = withContext(Dispatchers.IO) {
        val encodedWord = URLEncoder.encode(word.trim(), "UTF-8")
        val url = URL(
            "https://dict.minhqnd.com/api/v1/lookup?word=$encodedWord&lang=$lang&def_lang=$defLang"
        )

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext DictionaryLookupResult(
                    exists = false,
                    word = word,
                    results = emptyList()
                )
            }

            if (responseCode !in 200..299) {
                throw Exception("Dictionary API error: $responseCode")
            }

            val json = connection.inputStream.bufferedReader().use { it.readText() }
            parseLookupResponse(json)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseLookupResponse(json: String): DictionaryLookupResult {
        val root = JSONObject(json)
        val exists = root.optBoolean("exists", false)
        val word = root.optString("word", "")

        val resultsJson = root.optJSONArray("results")
        val results = mutableListOf<DictionaryLanguageResult>()

        if (resultsJson != null) {
            for (i in 0 until resultsJson.length()) {
                val item = resultsJson.getJSONObject(i)

                val meanings = mutableListOf<DictionaryMeaning>()
                val meaningsJson = item.optJSONArray("meanings")
                if (meaningsJson != null) {
                    for (j in 0 until meaningsJson.length()) {
                        val m = meaningsJson.getJSONObject(j)
                        meanings.add(
                            DictionaryMeaning(
                                definition = m.optString("definition", null),
                                definition_lang = m.optString("definition_lang", null),
                                example = m.optString("example", null),
                                pos = m.optString("pos", null),
                                sub_pos = m.optString("sub_pos", null),
                                source = m.optString("source", null),
                                links = emptyList()
                            )
                        )
                    }
                }

                val pronunciations = mutableListOf<DictionaryPronunciation>()
                val pronunciationsJson = item.optJSONArray("pronunciations")
                if (pronunciationsJson != null) {
                    for (j in 0 until pronunciationsJson.length()) {
                        val p = pronunciationsJson.getJSONObject(j)
                        pronunciations.add(
                            DictionaryPronunciation(
                                ipa = p.optString("ipa", null),
                                region = p.optString("region", null)
                            )
                        )
                    }
                }

                val translations = mutableListOf<DictionaryTranslation>()
                val translationsJson = item.optJSONArray("translations")
                if (translationsJson != null) {
                    for (j in 0 until translationsJson.length()) {
                        val t = translationsJson.getJSONObject(j)
                        translations.add(
                            DictionaryTranslation(
                                lang_code = t.optString("lang_code", null),
                                lang_name = t.optString("lang_name", null),
                                translation = t.optString("translation", null)
                            )
                        )
                    }
                }

                val relations = mutableListOf<DictionaryRelation>()
                val relationsJson = item.optJSONArray("relations")
                if (relationsJson != null) {
                    for (j in 0 until relationsJson.length()) {
                        val r = relationsJson.getJSONObject(j)
                        relations.add(
                            DictionaryRelation(
                                related_word = r.optString("related_word", null),
                                relation_type = r.optString("relation_type", null)
                            )
                        )
                    }
                }

                results.add(
                    DictionaryLanguageResult(
                        lang_code = item.optString("lang_code", null),
                        lang_name = item.optString("lang_name", null),
                        audio = item.optString("audio", null),
                        meanings = meanings,
                        pronunciations = pronunciations,
                        translations = translations,
                        relations = relations
                    )
                )
            }
        }

        return DictionaryLookupResult(
            exists = exists,
            word = word,
            results = results
        )
    }

    suspend fun suggestWords(
        query: String,
        limit: Int = 5
    ): List<String> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList<String>()

        val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
        val url = URL(
            "https://dict.minhqnd.com/api/v1/suggest?q=$encodedQuery&limit=$limit"
        )

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext emptyList<String>()
            }

            val json = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val suggestionsJson = root.optJSONArray("suggestions")
            val list = mutableListOf<String>()
            if (suggestionsJson != null) {
                for (i in 0 until suggestionsJson.length()) {
                    list.add(suggestionsJson.getString(i))
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }
}