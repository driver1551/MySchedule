package com.example.projectnailsschedule.di

import android.content.Context
import com.example.projectnailsschedule.utils.rustore.PaymentLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.RuStoreBillingClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentsModule {

    @Provides
    @Singleton
    fun provideRuStoreBillingClient(@ApplicationContext context: Context): RuStoreBillingClient {
        return RuStoreBillingClientFactory.create(
            context = context, // это ключевой элемент, который предоставляет информацию о текущем состоянии приложения или объекта
            consoleApplicationId = "2063509631", // идентификатор приложения из RuStore консоли.
            deeplinkScheme = "projectnailsschedule", // схема deeplink, необходимая для возврата в приложение после оплаты через стороннее приложение (например, SberPay или СБП). SDK генерирует свой хост к данной схеме.
            externalPaymentLoggerFactory = { PaymentLogger(tag = "projectnailsschedule") }, // интерфейс, позволяющий вести журнал событий
            themeProvider = null, // интерфейс, который предоставляет тему BillingClientTheme. Возможны 2 реализации темы BillingClientTheme: светлая (Light) и тёмная (Dark). Необязательный интерфейс, по умолчанию применяется светлая тема.
            debugLogs = true // флаг, регулирующий ведение журнала событий. Укажите значение true, если хотите, чтобы события попадали в журнал. В ином случае укажите false.
        )
    }
}

