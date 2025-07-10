package com.example.projectnailsschedule.utils.rustore

import android.content.Context

class RuStoreUpdate(val context: Context) {
    val log = "RuStoreUpdate"

    // TODO: Починить

//    fun checkForUpdates() {
//        val updateManager = RuStoreAppUpdateManagerFactory.create(context)
//        var appUpdateInfo: AppUpdateInfo?
//
//        updateManager
//            .getAppUpdateInfo()
//            .addOnSuccessListener { info ->
//                appUpdateInfo = info
//                Log.e(log, appUpdateInfo!!.updateAvailability.toString())
//                updateManager
//                    .startUpdateFlow(appUpdateInfo!!, AppUpdateOptions.Builder().build())
//                    .addOnSuccessListener { resultCode ->
//                        Log.e(log, resultCode.toString())
//                    }
//                    .addOnFailureListener { throwable ->
//                        Log.e(log, throwable.toString())
//                    }
//            }
//            .addOnFailureListener { throwable ->
//                Log.e(log, throwable.message!!)
//            }
//        updateManager.registerListener { state ->
//            if (state.installStatus == InstallStatus.DOWNLOADED) {
//                Log.e(log, "Update is ready to install")
//                updateManager
//                    .completeUpdate()
//                    .addOnFailureListener { throwable ->
//                        Log.e(log, throwable.message!!)
//                    }
//            }
//        }
//    }
}