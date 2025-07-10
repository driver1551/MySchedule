package com.example.projectnailsschedule.utils.rustore

import com.example.projectnailsschedule.domain.usecase.appointmentUC.SearchAppointmentUseCase
import javax.inject.Inject

/** Класс, который просит пользователя поставить оценку приложению
 * если достигнуто определенное к-во записей */

const val APPOINTMENTS_COUNT = 50

class RuStoreReview @Inject constructor(
    private val searchAppointmentUseCase: SearchAppointmentUseCase
) {

    // TODO: починить

    private val log = "RuStoreReview"

    private suspend fun getAllAppointmentsCount(): Int {
        return searchAppointmentUseCase.execute("").size
    }

//    suspend fun rateApp() {
//        val appCount = getAllAppointmentsCount()
//        if (appCount > APPOINTMENTS_COUNT) {
//            Log.e(log, "Appointments count > $APPOINTMENTS_COUNT. Ask for review")
//
//            // Запустить оценку приложения, только если пользователь создал APPOINTMENTS_COUNT записей
//            // Для работы с оценками необходимо создать RuStoreReviewManager с помощью RuStoreReviewManagerFactory:
//            val manager = RuStoreReviewManagerFactory.create(context)
//
//            // Вызовите requestReviewFlow() заранее перед вызовом launchReviewFlow(reviewInfo),
//            // чтобы подготовить необходимую информацию для отображения экрана.
//            // ReviewInfo имеет свой срок жизни — около пяти минут.
//            manager.requestReviewFlow()
//                .addOnCompleteListener(object : OnCompleteListener<ReviewInfo> {
//                    override fun onFailure(throwable: Throwable) {
//                        // Handle error
//                        // Если получен ответ onFailure, то не рекомендуем самостоятельно отображать ошибку пользователю,
//                        // так как пользователь не запускал данный процесс.
//                        Log.e(log, throwable.message.toString())
//                    }
//
//                    override fun onSuccess(result: ReviewInfo) {
//                        // Save reviewInfo
//                        // Если получен ответ onSuccess, то необходимо локально сохранить ReviewInfo,
//                        // для последующего вызова launchReviewFlow(reviewInfo).
//                        Log.e(log, "onSuccess")
//                        manager.launchReviewFlow(result) // Запуска формы запроса оценки и отзыва о приложении у пользователя
//                    }
//                })
//        } else {
//            Log.e(log, "Appointments count < $APPOINTMENTS_COUNT. Don`t ask for review")
//        }
//    }
}