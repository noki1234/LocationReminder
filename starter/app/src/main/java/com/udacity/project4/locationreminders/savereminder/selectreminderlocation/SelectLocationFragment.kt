package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.geofence.LocationPermissionsUtil
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback{

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private val _isPoiSelected = MutableLiveData<Boolean>()
    val isPoiSelected: LiveData<Boolean>
    get() = _isPoiSelected

    private var poi: PointOfInterest? = null

    private lateinit var map: GoogleMap
    private var focused: Boolean = false
    private lateinit var placeAutocomplete: AutocompleteSupportFragment
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapFragment: SupportMapFragment
    private val REQUEST_LOCATION_PERMISSION = 1


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        if (!Places.isInitialized()) {
            Places.initialize(context!!.applicationContext, "AIzaSyBUMiBf_WCKBqGJPZ82N3Jq6pljGK4r6dg")
        }



        placeAutocomplete = childFragmentManager.findFragmentById(R.id.place_autocomplete) as AutocompleteSupportFragment
        placeAutocomplete.setTypeFilter(TypeFilter.CITIES)
        placeAutocomplete.allowEnterTransitionOverlap = true
        placeAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        //    placeAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        placeAutocomplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                // Log.i(TAG, "Place: " + place.name + ", " + place.id)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                // Log.i(TAG, "An error occurred: $status")
            }
        })
        mapFragment = childFragmentManager.findFragmentById(R.id.map)  as SupportMapFragment
        mapFragment.getMapAsync(this)
        _isPoiSelected.value = false
        poi = null
        isPoiSelected.observe(viewLifecycleOwner, Observer { saveButton(it) })

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.saveBtn.setOnClickListener {
        onLocationSelected()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        enableMyLocation()
        googleMap.uiSettings.isZoomControlsEnabled = true
        setMapClick(map)
        setPoiClick(map)

        val zoomLevel = 13f
        if (isPermissionGranted() && LocationPermissionsUtil.isGpsEnabled(requireContext())) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (!focused && it != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), zoomLevel))
                    focused = true
                }
            }
        }

        map.setOnMyLocationButtonClickListener {
            if (!LocationPermissionsUtil.isGpsEnabled(requireContext())){
                LocationPermissionsUtil.checkDeviceLocationSettingsAndAskForTurnOnGps(requireActivity(), binding.locationConstraintLayout)
            }
            false
        }
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
       _viewModel.latitude.value = poi!!.latLng.latitude
        _viewModel.longitude.value = poi!!.latLng.longitude
        _viewModel.reminderSelectedLocationStr.value = poi!!.name
        _viewModel.selectedPOI.value = poi
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )

            val marker = map.addMarker(
                    MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.dropped_pin))
                            .snippet(snippet)
            )
            poi = PointOfInterest(marker.position, "dropped_pin", marker.title)
            _isPoiSelected.value = true
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
      val marker = map.addMarker(
              MarkerOptions()
                      .position(poi.latLng)
                      .title(poi.name)
      )
            marker!!.showInfoWindow()
            this.poi = poi
            _isPoiSelected.value = true
        }
    }

    private fun isPermissionGranted() : Boolean {
            return ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), getString(R.string.location_disbaled), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveButton(enable: Boolean){
        binding.saveBtn.isEnabled = enable
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.map_style
                    )
            )

        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ", e)
        }
    }






}
