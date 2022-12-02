package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject
import kotlin.coroutines.CoroutineContext

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */


// to listen for geofence transitions
// to handle the broadCast event that will be send by Geofence
class GeofenceBroadcastReceiver : BroadcastReceiver(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BroadcastReceiver","onReceive()")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            Log.d("BroadcastReceiver","geofencingEvent != null")
            if (geofencingEvent.hasError()) {
                Log.d("BroadcastReceiver","geofencingEvent.hasError()")
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent?.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d("BroadcastReceiver","enter geofenceTransition")

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.

            // Send notification
            geofencingEvent.triggeringGeofences?.forEach {
                Log.d("BroadcastReceiver","before sendNotification")
                sendGeofenceNotification(it.requestId, context)
                Log.d("BroadcastReceiver","after sendNotification")
            }
        }

    }



    //TODO: get the request id of the current geofence
    private fun sendGeofenceNotification(geofenceRequestId: String,context:Context) {

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject(ReminderDataSource::class.java)
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(geofenceRequestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notifica.tion to the user with the reminder details
                sendNotification(context,
                     ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }
}





