package com.udacity.project4

import java.util.concurrent.TimeUnit

object Constants {
    const val EMAIL_REGEX =  "(?:[a-z0-9!#$%&'*+\\=?^_`\\{|\\}~-]+(?:\\.[a-z0-9!#$%&'*+\\=?^_`\\{|\\}~-]+)*|(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*)@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z-]*[a-z]))"
    const val SIGN_IN_REQUEST_CODE = 1001
    const val GEOFENCE_RADIUS = 100f
    const val ACTION_GEOFENCE_EVENT= "SaveReminderFragment.savereminder.action.ACTION_GEOFENCE_EVENT"
    val GEOFENCE_EXPIRATION_TIME: Long = TimeUnit.HOURS.toMillis(1)
}