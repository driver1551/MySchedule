package com.example.projectnailsschedule.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectnailsschedule.domain.models.dto.UserInfoDto
import com.example.projectnailsschedule.domain.models.dto.UserInfoDtoManager
import com.example.projectnailsschedule.domain.usecase.account.GetJwt
import com.example.projectnailsschedule.domain.usecase.account.GetUserInfoApiUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.DisableSyncUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.serverSyncUC.EnableSyncUseCase
import com.example.projectnailsschedule.domain.usecase.rustore.CheckRuStoreLoginStatus
import com.example.projectnailsschedule.domain.usecase.rustore.GetPurchasesUseCase
import com.example.projectnailsschedule.domain.usecase.settingsUC.GetSpinnersStatusUseCase
import com.example.projectnailsschedule.domain.usecase.settingsUC.SetSpinnerStatusUseCase
import com.example.projectnailsschedule.presentation.ui.screens.settings.SettingsUiState
import com.example.projectnailsschedule.utils.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult
import ru.rustore.sdk.billingclient.utils.pub.checkPurchasesAvailability
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserInfoApi: GetUserInfoApiUseCase,
    private val getJwt: GetJwt,
    private val enableSyncUseCase: EnableSyncUseCase,
    private val disableSyncUseCase: DisableSyncUseCase,
    private val getRuStoreLoginStatus: CheckRuStoreLoginStatus,
    private val getPurchasesUseCase: GetPurchasesUseCase,
    private val getSpinnersStatusUseCase: GetSpinnersStatusUseCase,
    private val setSpinnerStatusUseCase: SetSpinnerStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsUiEvent>()
    val events = _events.asSharedFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userInfo = getUserInfoApi()
            val isAuthorizedRuStore = isAuthorizedRuStore()
            val isSubscriber = isSubscribed()
            val spinnerStatus = getSpinnersStatusUseCase.execute()

            val available = userInfo != null &&
                    (userInfo.betaTester == true || (isAuthorizedRuStore && isSubscriber))

            val enabled = userInfo?.syncEnabled == true

            _uiState.update {
                it.copy(
                    isLoading = false,
                    spinnersEnabled = spinnerStatus,
                    syncAvailable = available,
                    syncEnabled = enabled
                )
            }
        }
    }

    fun toggleSync() {
        viewModelScope.launch {
            if (!_uiState.value.syncAvailable) return@launch
            val current = _uiState.value.syncEnabled
            if (current) disableSync() else enableSync()

            _uiState.update { it.copy(syncEnabled = !current) }
        }
    }

    fun toggleSpinners() {
        val current = _uiState.value.spinnersEnabled
        if (current)
            setSpinnerStatusUseCase.invoke(false)
        else
            setSpinnerStatusUseCase.invoke(true)
        _uiState.update { it.copy(spinnersEnabled = !current) }
    }

    fun onSupportMailClicked() {
        viewModelScope.launch {
            _events.emit(SettingsUiEvent.SendSupportEmail)
        }
    }

    suspend fun getUserInfoApi(): UserInfoDto? {
        val jwt = getJwt.execute() ?: return null
        val username = Util().extractUsernameFromJwt(jwt) ?: return null
        val userInfo = getUserInfoApi.execute(username, jwt)?.body() ?: return null

        UserInfoDtoManager.setUserDto(userInfo)
        return userInfo
    }

    suspend fun enableSync(): Boolean {
        return try {
            enableSyncUseCase.execute(getUserInfoApi()!!, getJwt.execute()!!)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun disableSync(): Boolean {
        return try {
            disableSyncUseCase.execute(getUserInfoApi()!!, getJwt.execute()!!)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isAuthorizedRuStore(): Boolean {
        return viewModelScope.async {
            withContext(Dispatchers.IO) {
                getRuStoreLoginStatus.execute().await().authorized
            }
        }.await()
    }

    suspend fun isSubscribed(): Boolean {
        if (!canSafelyCheckPurchases()) return false
        // Проверяем, куплена ли подписка у пользователя
        return getPurchasesUseCase.execute().fold(
            onSuccess = {
                it.isNotEmpty()
            },
            onFailure = {
                false
            }
        )
    }

    private suspend fun canSafelyCheckPurchases(): Boolean = suspendCancellableCoroutine { cont ->
        RuStoreBillingClient.checkPurchasesAvailability()
            .addOnSuccessListener {
                cont.resume(it is PurchaseAvailabilityResult.Available) {}
            }
            .addOnFailureListener {
                cont.resume(false) {}
            }
    }
}