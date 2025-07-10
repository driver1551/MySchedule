package com.example.projectnailsschedule.presentation.settings

sealed class SettingsUiEvent {
    data object SendSupportEmail : SettingsUiEvent()
}