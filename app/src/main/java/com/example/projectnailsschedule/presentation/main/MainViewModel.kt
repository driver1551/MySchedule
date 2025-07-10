package com.example.projectnailsschedule.presentation.main

import androidx.lifecycle.ViewModel
import com.example.projectnailsschedule.domain.models.UserDataManager
import com.example.projectnailsschedule.domain.models.dto.UserInfoDto
import com.example.projectnailsschedule.domain.models.dto.UserInfoDtoManager
import com.example.projectnailsschedule.domain.usecase.account.GetJwt
import com.example.projectnailsschedule.domain.usecase.account.GetUserInfoApiUseCase
import com.example.projectnailsschedule.domain.usecase.apiUC.SendUserDataUseCase
import com.example.projectnailsschedule.utils.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sendUserDataUseCase: SendUserDataUseCase,
    private var getUserInfoApi: GetUserInfoApiUseCase,
    private var getJwt: GetJwt,
) : ViewModel() {

    suspend fun getUserInfoApi(): UserInfoDto? {
        val jwt = getJwt.execute() ?: return null
        val username = Util().extractUsernameFromJwt(jwt) ?: return null
        val userInfo = getUserInfoApi.execute(username, jwt)?.body() ?: return null

        UserInfoDtoManager.setUserDto(userInfo)
        return userInfo
    }

    fun sendUserData() {
        val userData = UserDataManager.pollUserDateQueue()
        if (userData != null) {
            sendUserDataUseCase.execute()
        }
    }
}