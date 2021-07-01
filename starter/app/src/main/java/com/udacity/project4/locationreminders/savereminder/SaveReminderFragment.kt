package com.udacity.project4.locationreminders.savereminder

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.BuildConfig.LIBRARY_PACKAGE_NAME
import androidx.viewbinding.BuildConfig.LIBRARY_PACKAGE_NAME
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceUtil
import com.udacity.project4.locationreminders.geofence.LocationPermissionsUtil
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var locationPermissionsUtil: LocationPermissionsUtil
    private lateinit var geofenceUtil : GeofenceUtil
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofenceUtil = GeofenceUtil(requireContext())
        locationPermissionsUtil = LocationPermissionsUtil(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
             checkDeviceLocationSettingsAndSaveReminderAndAddGeofence()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            addGeofenceAndSaveReminder()
        }
    }


    private fun addGeofenceAndSaveReminder() {

        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminder = ReminderDataItem(title, description, location, latitude, longitude)

        if (locationPermissionsUtil.foregroundAndBackgroundLocationPermissionApproved()){
            _viewModel.validateAndSaveReminder(reminder)
            GeofenceUtil(requireContext()).addGeofence(reminder)
        } else {
            locationPermissionsUtil.requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun checkDeviceLocationSettingsAndSaveReminderAndAddGeofence(resolve: Boolean = true) {
        val locationSettingsResponseTask = locationPermissionsUtil.checkDeviceLocationSettingsAndAskForTurnOnGps(binding.constrainLayout, resolve)
        locationSettingsResponseTask.addOnCompleteListener{
            if(it.isSuccessful) {
                addGeofenceAndSaveReminder()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        println("PERMISSION - LOCATION_PERMISSION_INDEX == DENIED: ${grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED}" )
        println("PERMISSION - requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE: ${requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE}" )
        println("PERMISSION - BACKGROUND_LOCATION_PERMISSION_INDEX == DENIED: ${grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED}" )
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED))
        {
            println("NOT Granted")
            //Snackbar explaining that the user needs location permissions in order to play
            Snackbar.make(
                binding.constrainLayout,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.settings) {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
        } else {  // Permissions are granted
            println("Granted")
            checkDeviceLocationSettingsAndSaveReminderAndAddGeofence()
        }
    }

}








