package com.example.projectnailsschedule.di

import com.example.projectnailsschedule.domain.usecase.apiUC.SendUserDataUseCase
import com.example.projectnailsschedule.utils.UncaughtExceptionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ExceptionHandlerModule {

    @Provides
    @Singleton
    fun provideUncaughtExceptionHandler(
        sendUserDataUseCase: SendUserDataUseCase
    ): Thread.UncaughtExceptionHandler {
        return UncaughtExceptionHandler(sendUserDataUseCase)
    }
}