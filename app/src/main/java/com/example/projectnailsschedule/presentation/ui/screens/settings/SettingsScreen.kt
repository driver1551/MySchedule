package com.example.projectnailsschedule.presentation.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectnailsschedule.presentation.settings.SettingsUiEvent
import com.example.projectnailsschedule.presentation.settings.SettingsViewModel
import com.example.projectnailsschedule.presentation.ui.components.SettingsItem
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.events.collectLatest { event ->
            when (event) {
                SettingsUiEvent.SendSupportEmail -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:scheduleapp@mail.ru".toUri()
                    }
                    context.startActivity(emailIntent)
                }
            }
        }
    }

    Column {
        SettingsItem(
            name = "Спиннеры",
            description = "Выбор даты в формате спиннера",
            onClick = {
                if (!state.isLoading) viewModel.toggleSpinners()
            },
            trailingContent = {
                Switch(
                    checked = state.spinnersEnabled,
                    onCheckedChange = null
                )
            }
        )

        SettingsItem(
            name = "Синхронизация",
            description = "Записи сохраняются в облаке (Premium)",
            onClick = {
                if (!state.isLoading) viewModel.toggleSync()
            },
            trailingContent = {
                Switch(
                    checked = state.syncEnabled,
                    enabled = state.syncAvailable,
                    onCheckedChange = null
                )
            }
        )

        SettingsItem(
            name = "Написать в поддержку",
            description = "scheduleapp@mail.ru",
            onClick = { viewModel.onSupportMailClicked() }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsScreenPreviewLight() {
    SettingsScreen()
}