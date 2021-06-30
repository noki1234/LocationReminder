package com.udacity.project4.locationreminders.geofence

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R


class LocationPermissionsUtil {

  
    
    companion object {
        private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.Q

        @TargetApi(29)
        fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
            //Check if the ACCESS_FINE_LOCATION permission is granted.
            val foregroundLocationApproved = (
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                            ))

//If the device is running Q or higher, check that the ACCESS_BACKGROUND_LOCATION permission is granted. Return true if the device is running lower than Q where you don't need a permission to access location in the background.
            val backgroundPermissionApproved =
                    if (runningQOrLater) { //for Q or
                        PackageManager.PERMISSION_GRANTED ==
                                ActivityCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                )
                    } else {
                        true
                    }
            return foregroundLocationApproved && backgroundPermissionApproved

        }

        fun requestForegroundAndBackgroundLocationPermissions(activty: Activity) {
            //If the permissions have already been approved, don’t need to ask again (return)
            if (foregroundAndBackgroundLocationPermissionApproved(activty))
                return
            //permissionsArray - contains the permissions that are going to be requested.
            //Initially, add ACCESS_FINE_LOCATION since that will be needed on all API levels.
            var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

            //resultCode - be different depending on if the device is running Q or later and will inform us if you need to check for one permission (fine location) or multiple permissions (fine & background location) when the user returns from the permission request screen.
            val resultCode = when {
                runningQOrLater -> { //the device is running Q or late
                    permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }
                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE //lower than Q
            }
            // Log.d(TAG, "Request foreground only location permission")
            ActivityCompat.requestPermissions(
                    activty,
                    permissionsArray,
                    resultCode
            )
        }


        fun checkDeviceLocationSettingsAndAskForTurnOnGps(activity: Activity, view: View, resolve: Boolean = true): Task<LocationSettingsResponse> {

            //Step 1: create a LocationRequest, a LocationSettingsRequest Builder.
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

            //Step 2: Use LocationServices to get the Settings Client and create a val called locationSettingsResponseTask to check the location settings
            val settingsClient = LocationServices.getSettingsClient(activity)
            val locationSettingsResponseTask =
                    settingsClient.checkLocationSettings(builder.build())

            //Step 3: add an onFailureListener() to the locationSettingsResponseTask
            locationSettingsResponseTask.addOnFailureListener { exception ->
                // if the exception is of type ResolvableApiException -> call startResolutionForResult() - to prompt the user to turn on device location.
                if (exception is ResolvableApiException && resolve) {
                    try {
                        println("KUK")
                   //     startIntentSenderForResult(activity, exception.resolution.intentSender, REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0,null)
//                        exception.startResolutionForResult(activity,
//                                REQUEST_TURN_DEVICE_LOCATION_ON)
                        startIntentSenderForResult(
                            activity,
                            exception.resolution.intentSender,
                            REQUEST_TURN_DEVICE_LOCATION_ON,
                            null,
                            0,
                            0,
                            0,
                            null
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.d("ReminderListFragment: ", "Error getting location settings resolution: " + sendEx.message)
                    }
                    //exception is not of type ResolvableApiException -> present a snackbar that alerts the user that location needs to be enabled to play the treasure hunt.
                } else {
                    Snackbar.make(
                            view,
                            R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {
                        checkDeviceLocationSettingsAndAskForTurnOnGps(activity, view, resolve)
                    }.show()
                }
            }

            return locationSettingsResponseTask

        }



        fun isGpsEnabled(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // This is a new method provided in API 28
                val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                return manager.isLocationEnabled
            } else {
                // This was deprecated in API 28
                val mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF);
                return (mode != Settings.Secure.LOCATION_MODE_OFF);
            }
        }




    }

}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
