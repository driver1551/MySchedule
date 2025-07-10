package com.example.projectnailsschedule.utils.rustore

import android.content.Context
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.projectnailsschedule.domain.usecase.rustore.GetPurchasesUseCase
import com.my.target.ads.InterstitialAd
import com.my.target.ads.MyTargetView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.rustore.sdk.billingclient.BuildConfig
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.purchase.PurchaseAvailabilityResult
import ru.rustore.sdk.billingclient.utils.pub.checkPurchasesAvailability
import javax.inject.Inject

class RuStoreAd @Inject constructor(
    private val getPurchasesUseCase: GetPurchasesUseCase
) {
    val log = "Ads"

    // Banner
    lateinit var adView: MyTargetView

    // InterstitialAd
    private lateinit var interstitialAd: InterstitialAd

    fun interstitialAd(context: Context, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            if (isSubscribed()) {
                Log.i("RuStoreAd", "Подписка найдена, не показываем interstitialAd")
                return@launch
            }
            Log.i("RuStoreAd", "Подписка не найдена, показываем interstitialAd")

            val log = "InterstitialAd"

            val slotId = 1285135

            // Создаем экземпляр InterstitialAd
            interstitialAd = InterstitialAd(slotId, context)

            // Устанавливаем слушатель событий
            interstitialAd.setListener(object : InterstitialAd.InterstitialAdListener {
                override fun onLoad(ad: InterstitialAd) {
                    // Запускаем показ в отдельном Activity
                    Log.e(log, "onLoad")
                    ad.show()
                }

                override fun onNoAd(reason: String, ad: InterstitialAd) {
                    Log.e(log, "onNoAd")
                }

                override fun onClick(ad: InterstitialAd) {}
                override fun onDisplay(ad: InterstitialAd) {}
                override fun onDismiss(ad: InterstitialAd) {}
                override fun onVideoCompleted(ad: InterstitialAd) {}
            })

            // Запускаем загрузку данных
            if (BuildConfig.DEBUG) {
                Log.d(log, "Debug")
            } else {
                Log.d(log, "Prod")
                interstitialAd.load()
            }
        }
    }

    fun banner(
        context: Context,
        layout: ConstraintLayout,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            if (isSubscribed()) {
                Log.i("RuStoreAd", "Подписка найдена, не показываем banner")
                return@launch
            }
            Log.i("RuStoreAd", "Подписка не найдена, показываем banner")

            val log = "BannerAd"

            val slotId = 1404141

            // Включение режима отладки
            // MyTargetManager.setDebugMode(true)

            /*        GlobalScope.launch {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                Log.d("text", adInfo.id ?: "unknown")
            }*/

            // Создаем экземпляр MyTargetView
            adView = MyTargetView(context)

            // Задаём id слота
            adView.setSlotId(slotId)

            val adViewLayoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            adViewLayoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            adViewLayoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            adViewLayoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

            adView.layoutParams = adViewLayoutParams

            adView.listener = object : MyTargetView.MyTargetViewListener {
                override fun onLoad(myTargetView: MyTargetView) {
                    // Данные успешно загружены, запускаем показ объявлений
                    Log.e(log, "onLoad")
                    layout.addView(adView)
                }

                override fun onNoAd(reason: String, myTargetView: MyTargetView) {
                    Log.e(log, "onNoAd")
                }

                override fun onShow(myTargetView: MyTargetView) {
                    Log.e(log, "onShow")
                }

                override fun onClick(myTargetView: MyTargetView) {
                    Log.e(log, "onClick")
                }
            }

            // Запускаем загрузку данных
            if (BuildConfig.DEBUG) {
                Log.d(log, "Debug")
            } else {
                // Код для продакшн версии
                Log.d(log, "Prod")
                adView.load()
            }
        }
    }

    fun destroyAd() {
        adView.destroy()
        interstitialAd.destroy()
    }

    private suspend fun isSubscribed(): Boolean {
        // Проверяем, куплена ли подписка у пользователя
        delay(5000) // Чтобы слишком часто не запрашивал статус, потом убрать

        if (!canSafelyCheckPurchases()) {
            return false
        }

        return getPurchasesUseCase.execute().fold(
            onSuccess = {
                it.isNotEmpty()
            },
            onFailure = {
                false
            }
        )
    }

    private suspend fun canSafelyCheckPurchases(): Boolean {
        return suspendCancellableCoroutine { cont ->
            RuStoreBillingClient.checkPurchasesAvailability()
                .addOnSuccessListener { result ->
                    val isAvailable = result is PurchaseAvailabilityResult.Available
                    cont.resume(isAvailable) {}
                }
                .addOnFailureListener {
                    cont.resume(false) {}
                }
        }
    }
}