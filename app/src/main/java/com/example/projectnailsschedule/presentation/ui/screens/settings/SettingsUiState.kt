package com.example.projectnailsschedule.presentation.ui.screens.settings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val spinnersEnabled: Boolean = false,
    val syncAvailable: Boolean = false,
    val syncEnabled: Boolean = false,
)