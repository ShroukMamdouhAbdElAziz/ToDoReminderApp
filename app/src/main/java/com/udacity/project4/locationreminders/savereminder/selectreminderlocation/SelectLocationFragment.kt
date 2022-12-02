package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentSelectLocationBinding

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()

    // googleMaps
    private lateinit var map: GoogleMap
    private var mapMarker: Marker? = null


    // using Activity result API to determine which permissions the system hs granted to the app
    private val ForegroundLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||

                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                enableCurrentLocationSettings()
            } else {
                _viewModel.showSnackBarInt.value = R.string.location_denied
            }
        }

    private val locationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            enableCurrentLocationSettings()
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this


        // Obtain the SupportMapFragment to get the  Google Map into my App and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // getMapAsync is also called to prepare the Google Map
        // Then when this finishes , the onMapReady() is called.
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)

        setDisplayHomeAsUpEnabled(true)
        requestLocationPermissions()

        initClickListener()

        return binding.root
    }


    fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .setMaxUpdateDelayMillis(500)
            .setDurationMillis(1000)
            .setWaitForAccurateLocation(false)
            .build()
    }

    fun enableCurrentLocationSettings() {
        val getLocationRequet = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(getLocationRequet)

        // check whether the current location settings are statisfied
        val locationSettingsResponse = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())

        locationSettingsResponse.addOnSuccessListener {
            // All location settings are satisfied
            getDeviceLastKnownLocation()
        }

        locationSettingsResponse.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    locationRequestLauncher.launch(
                        IntentSenderRequest.Builder(it.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            } else {
                _viewModel.showSnackBarInt.value = R.string.location_disabled
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLastKnownLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationResult: Task<Location> = fusedLocationClient.lastLocation

        locationResult.addOnCompleteListener {
            val lastKnownLocation = it.result

            if (it.isSuccessful) {
                map.uiSettings.isMyLocationButtonEnabled = true

                // in case lastKnownLocatio  is not Null
                if (lastKnownLocation != null) {
                    moveCameraLocation(lastKnownLocation)
                }
                //in case lastKnownLocation is Null
                else {
                    val locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            val lastKnownLocation = locationResult.lastLocation
                            if (lastKnownLocation != null) {
                                moveCameraLocation(lastKnownLocation)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    " No location detected",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                    }
                    // update the location
                    fusedLocationClient.requestLocationUpdates(
                        createLocationRequest(),
                        locationCallback,
                        Looper.myLooper()
                    )
                }
            } else {
                map.uiSettings.isMyLocationButtonEnabled = false
            }
        }
    }


    private fun moveCameraLocation(location: Location) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), 15f
            )
        )

    }

    private fun requestForegroundLocationPermissions() {
        val shouldShowRequestRationale =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestRationale) {
            displayLocationPermissionsDialog()
        } else {
            ForegroundLocationPermissions.launch(LOCATION_PERMISSIONS)
        }
    }

    private fun displayLocationPermissionsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(R.string.permission_rationale_dialog)
            .setPositiveButton("Accept") { _, _ ->
                ForegroundLocationPermissions.launch(LOCATION_PERMISSIONS)
            }
            .show()
    }

    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestForegroundLocationPermissions()
        } else {
            enableCurrentLocationSettings()
        }
    }

    fun initClickListener() {
        binding.saveButton.setOnClickListener {
            if (mapMarker != null) {
                _viewModel.runSelectLocation(mapMarker!!.position, mapMarker!!.title)
                _viewModel.navigationCommand.value = NavigationCommand.Back

            } else {
                _viewModel.showSnackBarInt.value = R.string.err_select_location
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // change the map type based on the user's selection
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
        }
        return true
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        val lattitude = 31.251980417667017
        val longitude = 29.974141512614484

        // to control how zoomed in the map
        val zoomLevel = 15f


        // Add a marker in this location and move the camera
        val homeLatLng = LatLng(lattitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.moveCamera(CameraUpdateFactory.newLatLng(homeLatLng))
        // customize the styling of the base map using a JSON object
        // defined in a raw resource file
        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)


    }


    private fun setMapStyle(map: GoogleMap) {

        // customize the styling of the base map using a JSON object
        // defined in a raw resource file
        val success = map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
        )

    }

    private fun setMarker(latLng: LatLng, map: GoogleMap): Marker? {
        // to diplay the coordinates of selected location
        val snippet = String.format(
            Locale.getDefault(), "Lat: %1$.5f , Long: %2$.5f", latLng.latitude, latLng.longitude
        )
        var markerOptions = MarkerOptions().position(latLng)
            .title(getString(R.string.dropped_pin)).snippet(snippet)
            // to customize the color of marker for long pressed
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        return map.addMarker(markerOptions)
    }

    //to add marker when the user touches and holds on the map to place a marker at a location on the map
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapClickListener {

            // remove the old marker and geofence circle

            //   mapMarker?.remove()
            map.clear()

            mapMarker = setMarker(it, map)


        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            mapMarker?.remove()
            mapMarker = map.addMarker(
                MarkerOptions().position(it.latLng).title(it.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )

            mapMarker?.showInfoWindow()

        }

    }


    companion object {

        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

}
