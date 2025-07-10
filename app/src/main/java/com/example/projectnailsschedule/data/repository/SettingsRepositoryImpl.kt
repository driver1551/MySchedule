package com.example.projectnailsschedule.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.projectnailsschedule.domain.repository.repo.SettingsRepository

const val CUSTOM_PREF_NAME = "Settings"
const val themeKey = "theme"
const val monthKey = "month"
const val languageKey = "language"
const val userTheme = "theme"
const val jwtKey = "jwt"
const val updateKeyAppointments = "updateKeyAppointments"
const val updateKeyCalendarDate = "updateKeyCalendarDate"
const val spinnerStatus = "spinnerStatus"

class SettingsRepositoryImpl(context: Context?) : SettingsRepository {

    private val sharedPreference: SharedPreferences =
        context!!.getSharedPreferences(CUSTOM_PREF_NAME, Context.MODE_PRIVATE)
    private val defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context!!)

    override fun setDarkTheme() {
        sharedPreference.edit {
            putBoolean(themeKey, true)
        }
    }

    override fun setLightTheme() {
        sharedPreference.edit {
            putBoolean(themeKey, false)
        }
    }

    override fun loadTheme(): Boolean {
        return sharedPreference.getBoolean(themeKey, false)
    }

    override fun setLanguage(language: String) {
        sharedPreference.edit {
            putString(monthKey, language)
        }
    }

    override fun getLanguage(): String {
        return sharedPreference.getString(languageKey, "English")!!
    }

    override fun setUserTheme(theme: String) {
        sharedPreference.edit {
            putString(userTheme, theme)
        }
    }

    override fun getUserTheme(): String {
        val defaultTheme = "Theme.Main"
        return sharedPreference.getString(userTheme, defaultTheme)!!
    }

    override fun setJwt(jwt: String?): Boolean {
        sharedPreference.edit {
            putString(jwtKey, jwt)
        }
        return true
    }

    override fun getJwt(): String? {
        return sharedPreference.getString(jwtKey, null)
    }

    override fun setAppointmentsLastUpdate(time: Long) {
        sharedPreference.edit {
            putString(updateKeyAppointments, time.toString())
        }
    }

    override fun getAppointmentsLastUpdate(): Long {
        return sharedPreference.getString(updateKeyAppointments, null)?.toLong() ?: 0L
    }

    override fun setCalendarDateLastUpdate(time: Long) {
        sharedPreference.edit {
            putString(updateKeyCalendarDate, time.toString())
        }
    }

    override fun getCalendarDateLastUpdate(): Long {
        return sharedPreference.getString(updateKeyCalendarDate, null)?.toLong() ?: 0L
    }

    override fun getSpinnerStatus(): Boolean {
        return sharedPreference.getBoolean(spinnerStatus, false)
    }

    override fun setSpinnerStatus(status: Boolean) {
        sharedPreference.edit {
            putBoolean(spinnerStatus, status)
        }
    }
}