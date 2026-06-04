package vn.edu.hcmute.minlish.data.remote.dictionary

data class DictionaryLookupResult(
    val exists: Boolean,
    val word: String?,
    val results: List<DictionaryLanguageResult>?
)

data class DictionaryLanguageResult(
    val lang_code: String?,
    val lang_name: String?,
    val audio: String?,
    val meanings: List<DictionaryMeaning>?,
    val pronunciations: List<DictionaryPronunciation>?,
    val translations: List<DictionaryTranslation>?,
    val relations: List<DictionaryRelation>?
)

data class DictionaryMeaning(
    val definition: String?,
    val definition_lang: String?,
    val example: String?,
    val pos: String?,
    val sub_pos: String?,
    val source: String?,
    val links: List<String>?
)

data class DictionaryPronunciation(
    val ipa: String?,
    val region: String?
)

data class DictionaryTranslation(
    val lang_code: String?,
    val lang_name: String?,
    val translation: String?
)

data class DictionaryRelation(
    val related_word: String?,
    val relation_type: String?
)