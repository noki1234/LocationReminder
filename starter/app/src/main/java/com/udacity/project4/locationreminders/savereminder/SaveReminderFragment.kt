package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var geofenceUtil : GeofenceUtil
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        geofenceUtil = GeofenceUtil(requireContext())

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
        println("WHY IS THIS NOT CALLING?")
       super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            println("INSIDE")
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

        if (LocationPermissionsUtil.foregroundAndBackgroundLocationPermissionApproved(requireActivity())){
            _viewModel.validateAndSaveReminder(reminder)
            GeofenceUtil(requireContext()).addGeofence(reminder)
        } else {
            LocationPermissionsUtil.requestForegroundAndBackgroundLocationPermissions(requireActivity())
        }
    }

    private fun checkDeviceLocationSettingsAndSaveReminderAndAddGeofence(resolve: Boolean = true) {
        val locationSettingsResponseTask = LocationPermissionsUtil.checkDeviceLocationSettingsAndAskForTurnOnGps(requireActivity(), binding.constrainLayout, resolve)
        locationSettingsResponseTask.addOnCompleteListener{
                addGeofenceAndSaveReminder()
        }
    }



}








