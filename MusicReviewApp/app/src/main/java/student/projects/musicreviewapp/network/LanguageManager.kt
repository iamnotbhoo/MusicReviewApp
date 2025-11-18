package student.projects.musicreviewapp.network

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class LanguageManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MusicReviewApp", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val DEFAULT_LANGUAGE = "en"
    }

    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("en", "English", "English"),
            Language("af", "Afrikaans", "Afrikaans"),
            Language("xh", "Xhosa", "isiXhosa")
        )
    }

    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(languageCode: String) {
        sharedPreferences.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()
        updateAppLocale(languageCode)
    }

    fun getCurrentLanguageDisplayName(): String {
        return when (getCurrentLanguage()) {
            "en" -> "English"
            "af" -> "Afrikaans"
            "xh" -> "Xhosa"
            else -> "English"
        }
    }

    private fun updateAppLocale(languageCode: String) {
        val locale = when (languageCode) {
            "af" -> Locale("af")
            "xh" -> Locale("xh")
            else -> Locale.ENGLISH
        }

        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String
    )
}