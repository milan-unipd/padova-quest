package it.unipd.milan.padovaquest.shared_quests.presentation.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import it.unipd.milan.padovaquest.shared_quests.domain.model.Place
import it.unipd.milan.padovaquest.shared_quests.domain.use_case.PlacesUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuestService : Service() {


    @Inject
    lateinit var sharedQuestDataRepository: SharedQuestDataRepository

    @Inject
    lateinit var placesUseCases: PlacesUseCases


    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        var isRunning = false
        var isInForeground = false
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        isRunning = true
        NotificationHelper.createNotificationChannel(this)

        val notification = NotificationHelper.getServiceNotification(this).build()
        startForeground(NotificationHelper.SERVICE_NOTIFICATION_ID, notification)


        serviceScope.launch {
            launch {
                sharedQuestDataRepository.startLocationUpdates(5000L)
            }

            launch {
                var lastNearestPlace: Place? = null

                launch {
                    sharedQuestDataRepository.locationFlow.collect { locationStatus ->
                        if (!locationStatus.isLocationEnabled || locationStatus.location == null || sharedQuestDataRepository.quest?.finishedOn != null)
                            return@collect

                        val quest = sharedQuestDataRepository.quest
                        val nearestPlace = placesUseCases.getNearestPlaceUseCase(locationStatus.location, quest)
                        sharedQuestDataRepository.emmitNearestPlaceFlow(nearestPlace)

                        if (quest?.id != null) {
                            if (!isInForeground) {
                                if (nearestPlace == null) {
                                    NotificationHelper.cancelNotification(this@QuestService, NotificationHelper.TRIVIA_NOTIFICATION_ID)
                                    return@collect
                                }
                                if (nearestPlace.id != lastNearestPlace?.id) {
                                    lastNearestPlace = nearestPlace
                                    NotificationHelper.notify(this@QuestService,
                                        "Hey, you are near ${nearestPlace.name}!\n" +
                                                "Tap to answer trivia question",
                                        NotificationHelper.TRIVIA_NOTIFICATION_ID
                                    )
                                }
                            }

                        } else {
                            if (!isInForeground) {
                                if (nearestPlace == null) {
                                    NotificationHelper.cancelNotification(this@QuestService, NotificationHelper.WALK_NOTIFICATION_ID)
                                    return@collect
                                }
                                if (nearestPlace.id != lastNearestPlace?.id) {
                                    lastNearestPlace = nearestPlace
                                    NotificationHelper.notify(this@QuestService,
                                        "Hey, you are near ${nearestPlace.name}!\n" +
                                                "Tap to see more about it",
                                        NotificationHelper.WALK_NOTIFICATION_ID
                                    )
                                }
                            }
                        }
                    }
                }
                launch {
                    sharedQuestDataRepository.groupQuestStatus.collect { status ->
                        if (!isInForeground) {
                            if (status.hasStarted) {
                                NotificationHelper.notify(this@QuestService, "Hey! The Group Quest you have join has started!", NotificationHelper.GROUP_QUEST_NOTIFICATION_ID)
                            }

                            if (status.wasDeleted) {
                                NotificationHelper.notify(this@QuestService, "The Group Quest you have join has been cancelled!", NotificationHelper.GROUP_QUEST_NOTIFICATION_ID)
                            }
                        }

                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sharedQuestDataRepository.stop()
        serviceScope.cancel()
        isRunning = false
    }

}