package com.example.projectnailsschedule.domain.models.dto

data class UserInfoDto(
    val id: Long?,
    val username: String?,
    val userEmail: String?,
    val emailVerified: Boolean?,
    val enabled: Boolean?,
    val syncEnabled: Boolean?,
    val accountNonLocked: Boolean?,
    val authorities: List<String>?,
    val credentialsNonExpired: Boolean?,
    val accountNonExpired: Boolean?,
    val betaTester: Boolean?
)

object UserInfoDtoManager {
    private var userInfoDto: UserInfoDto? = null

    // Возвращает текущий UserDto или null, если он не установлен
    fun getUserDto(): UserInfoDto? {
        return userInfoDto
    }

    // Устанавливает UserDto
    fun setUserDto(newUserInfoDto: UserInfoDto) {
        userInfoDto = newUserInfoDto
    }

    // Очищает сохраненный UserDto
    fun clearUserDto() {
        userInfoDto = null
    }
}