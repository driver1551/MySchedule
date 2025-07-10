package com.example.projectnailsschedule.domain.repository.repo

interface SettingsRepository {

    fun setDarkTheme()

    fun setLightTheme()

    fun loadTheme(): Boolean

    fun setLanguage(language: String)

    fun getLanguage(): String

    fun setUserTheme(theme: String)

    fun getUserTheme(): String

    fun setJwt(jwt: String?): Boolean

    fun getJwt(): String?

    fun setAppointmentsLastUpdate(time: Long)

    fun getAppointmentsLastUpdate(): Long

    fun setCalendarDateLastUpdate(time: Long)

    fun getCalendarDateLastUpdate(): Long

    fun getSpinnerStatus(): Boolean

    fun setSpinnerStatus(status: Boolean)
}