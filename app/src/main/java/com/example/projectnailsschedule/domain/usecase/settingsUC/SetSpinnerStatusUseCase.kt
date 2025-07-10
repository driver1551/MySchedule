package com.example.projectnailsschedule.domain.usecase.settingsUC

import com.example.projectnailsschedule.domain.repository.repo.SettingsRepository

class SetSpinnerStatusUseCase(private val repository: SettingsRepository) {
    operator fun invoke(status: Boolean) {
        repository.setSpinnerStatus(status)
    }
}