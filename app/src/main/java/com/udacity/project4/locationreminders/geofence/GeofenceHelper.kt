package com.udacity.project4.locationreminders.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import java.nio.file.attribute.AclEntry.Builder

class GeofenceHelper(base: Context) : ContextWrapper(base) {


    fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {

        return GeofencingRequest.Builder().addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

    }

    fun getGeofence(
        geofenceID: String,
        latLng: LatLng,
        radius: Float,
        transitionTypes: Int
    ): Geofence {
        return Geofence.Builder().setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setRequestId(geofenceID)
            .setTransitionTypes(transitionTypes)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

    }

    fun getPendingIntent(): PendingIntent {


        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )


        return pendingIntent


    }


    fun getErrorString(e: ApiException): String {
        when (e.statusCode) {

            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> return "GEOFENCE_NOT_AVAILABLE"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> return "GEOFENCE_TOO_MANY_GEOFENCES"
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> return "GEOFENCE_TOO_MANY_PENDING_INTENTS"
        }
        return e.localizedMessage

    }

    companion object {
        private const val TAG = "GEOFENCE_HELPER"
        private const val REQUEST_CODE = 2607

    }


}

