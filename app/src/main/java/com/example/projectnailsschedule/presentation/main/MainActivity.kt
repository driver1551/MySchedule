package com.example.projectnailsschedule.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.data.worker.SyncWorker
import com.example.projectnailsschedule.databinding.ActivityMainBinding
import com.example.projectnailsschedule.domain.models.UserDataManager
import com.example.projectnailsschedule.utils.rustore.RuStoreAd
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.my.target.ads.MyTargetView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val log = this::class.simpleName
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler

    private var drawerLayout: DrawerLayout? = null
    private var navView: NavigationView? = null

    @Inject
    lateinit var ruStoreAd: RuStoreAd

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus == InstallStatus.DOWNLOADED) {
            Toast.makeText(
                applicationContext,
                R.string.update_app_toast,
                Toast.LENGTH_LONG
            ).show()
        }
        lifecycleScope.launch {
            delay(5.seconds)
            appUpdateManager.completeUpdate()
        }
    }

    @Inject
    lateinit var billingClient: RuStoreBillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lifecycleObserver = AppLifecycleObserver()
        lifecycle.addObserver(lifecycleObserver)

        // set UserData singleton
        CoroutineScope(Dispatchers.IO).launch {
            mainViewModel.getUserInfoApi()
        }

        // Set uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // init all widgets
        initWidgets()

        // configure app bar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_calendar
                // в какие фрагменты устанавливать меню
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView?.setupWithNavController(navController)

        // check for updates
        // RuStoreUpdate(this).checkForUpdates()
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
        checkForUpdates()

        // start advertising
        ruStoreAd.adView = MyTargetView(this)

        ruStoreAd.interstitialAd(context = applicationContext, coroutineScope = lifecycleScope)

        initObservers()

        if (savedInstanceState == null) {
            billingClient.onNewIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        billingClient.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this,
                        123
                    )
                }
            }
        }

        // Start work manager
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "SyncWorkerUnique",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    private fun initWidgets() {
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> navController.navigate(R.id.nav_search)
            R.id.full_month_view -> navController.navigate(R.id.fullMonthViewFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        ruStoreAd.destroyAd()
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }

    private fun checkForUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }

            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    123
                )
            }
        }
    }

    private fun initObservers() {
        // Подключаем Observer к userDateQueue
        UserDataManager.userDateQueue.observe(this) { userDataQueue ->
            // Обрабатываем обновления userDateQueue
            CoroutineScope(Dispatchers.IO).launch {
                mainViewModel.sendUserData()
            }
        }
    }
}