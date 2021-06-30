package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.geofence.GeofenceUtil
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.android.synthetic.main.activity_reminder_description.*

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {
    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
        private const val EXTRA_GeofenceRequestID = "EXTRA_GeofenceRequestID"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem, geofenceId: String): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            intent.putExtra(EXTRA_GeofenceRequestID, geofenceId)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var latLng: LatLng
    private lateinit var geofenceUtil: GeofenceUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
//        TODO: Add the implementation of the reminder details
       val reminderDataItem =  intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        val geofenceId =  intent.getStringExtra(EXTRA_GeofenceRequestID)

        binding.reminderDataItem = reminderDataItem
        latLng = LatLng(reminderDataItem.latitude!!, reminderDataItem.longitude!!)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map)  as SupportMapFragment
        mapFragment.getMapAsync(this)
        geofenceUtil = GeofenceUtil(this)

        closeBtn.setOnClickListener {
            finish()
        }

        removeGeofenceBtn.setOnClickListener{
            geofenceUtil.removeGeofence(geofenceId!!)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        googleMap.uiSettings.isZoomControlsEnabled = true
        val zoomLevel = 16f

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latLng.latitude, latLng.longitude), zoomLevel))
        map.addMarker(MarkerOptions().position(latLng))
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.map_style
                )
            )

        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ", e)
        }
    }


}
