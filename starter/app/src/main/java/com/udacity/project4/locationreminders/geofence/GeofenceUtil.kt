package com.udacity.project4.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.Constants
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem


class GeofenceUtil(var context: Context){


    private var geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    private val geofencePendingIntent: PendingIntent by lazy {
        intent.action = "SaveReminderFragment.savereminder.action.ACTION_GEOFENCE_EVENT"
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    @SuppressLint("MissingPermission")
    fun addGeofence(reminder: ReminderDataItem) {
        if (reminder.location.isNullOrEmpty()) {
            return
        }
        //Build the geofence (via builder), using the informations in currentGeofenceData (id, lat&long)
        val geofenceBuilder = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(reminder.latitude!!,
                    reminder.longitude!!,
                    Constants.GEOFENCE_RADIUS//geofence radius
            )
            // Set the expiration duration using the constant set in GeofencingConstants.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        //Build the geofence request, set the initial trigger to INITIAL_TRIGGER_ENTER,
        // add the geofence you just built and then build.
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofenceBuilder)
            .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                //If adding the geofences is successful -> show toast that they were successful.
                addOnSuccessListener {
                    Log.e("Add Geofence", geofenceBuilder.requestId)
                }
                //If adding the geofences is fail -> show toast that there was a problem.
                addOnFailureListener {
                    if ((it.message != null)) {
                        Log.w("SaveReminderF-Geofence:", it.message!!)
                    }
                }
        }
    }

    fun removeGeofence(geofenceId: String) {
        geofencingClient.removeGeofences(listOf(geofenceId))?.run {
            addOnSuccessListener { //in case of success removing
                Log.d("GeofenceUtil", context.getString(R.string.geofences_removed))
                Toast.makeText(
                        context,
                        context.getString(R.string.geofences_removed),
                        Toast.LENGTH_SHORT
                )
                    .show()
            }
            addOnFailureListener { ////in case of failure
                Toast.makeText(
                        context,
                        context.getString(R.string.geofence_not_removed),
                        Toast.LENGTH_SHORT
                )
                    .show()
                Log.d("GeofenceUtil", context.getString(R.string.geofence_not_removed))
            }
        }

    }



}