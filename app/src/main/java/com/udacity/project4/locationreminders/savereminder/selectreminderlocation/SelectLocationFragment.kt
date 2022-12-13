package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
            // user refused to enable current location
            if (it.resultCode == Activity.RESULT_CANCELED) {
                // enableCurrentLocationSettings()
                _viewModel.showToast.value =
                    "For better experience you should enable your current location to get it on the map "

            } else if (it.resultCode == Activity.RESULT_OK) {
                getDeviceLastKnownLocation()
            }

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


        initClickListener()

        return binding.root
    }


    fun createLocationRequest(): LocationRequest {
        Log.d("select Fragment", "createLocationRequest()")
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

    }


    fun enableCurrentLocationSettings() {
        Log.d("select Fragment", "enableCurrentLocationSettings()")
        val getLocationRequet = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(getLocationRequet)

        // check whether the current location settings are statisfied
        val locationSettingsResponse = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())

        locationSettingsResponse.addOnSuccessListener {
            // All location settings are satisfied
            Log.d("select Fragment", "locationSettingsResponse.addOnSuccessListener")
            getDeviceLastKnownLocation()
        }

        locationSettingsResponse.addOnFailureListener {
            if (it is ResolvableApiException) {

                locationRequestLauncher.launch(
                    IntentSenderRequest.Builder(it.resolution.intentSender).build()
                )

            } else {
                _viewModel.showSnackBarInt.value = R.string.location_disabled
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLastKnownLocation() {
        Log.d("select Fragment", "getDeviceLastKnownLocation()")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val locationResult: Task<Location> = fusedLocationClient.lastLocation

        // to show the current location icon only
        map.isMyLocationEnabled = true

        locationResult.addOnCompleteListener {
            val lastKnownLocation = it.result

            if (it.isSuccessful) {
                map.isMyLocationEnabled = true

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
                map.isMyLocationEnabled = false
            }
        }
    }


    private fun moveCameraLocation(location: Location) {
        Log.d("select Fragment", "moveCameraLocation()")
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
        Log.d("select Fragment", "requestForegroundLocationPermissions()")
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
        Log.d("select Fragment", "displayLocationPermissionsDialog()")
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(R.string.permission_rationale_dialog)
            .setPositiveButton("Accept") { _, _ ->
                ForegroundLocationPermissions.launch(LOCATION_PERMISSIONS)
            }
            .show()
    }

    private fun requestLocationPermissions() {
        Log.d("select Fragment", "requestLocationPermissions()")
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
        Log.d("select Fragment", "initClickListener()")
        binding.saveButton.setOnClickListener {
            if (mapMarker != null) {
                Log.d("select Fragment", "$mapMarker")
                _viewModel.runSelectLocation(mapMarker!!.position, mapMarker!!.title)
                _viewModel.navigationCommand.value = NavigationCommand.Back

            } else {
                _viewModel.showSnackBarInt.value = R.string.err_select_location
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
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
        Log.d("select Fragment", "onMapReady()")
        map = googleMap

        // customize the styling of the base map using a JSON object
        // defined in a raw resource file
        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        Log.d("select Fragment", "requestLocationPermissions() callback in onMapReady before")
        requestLocationPermissions()
        Log.d("select Fragment", "requestLocationPermissions() callback in onMapReady after")


    }


    private fun setMapStyle(map: GoogleMap) {

        // customize the styling of the base map using a JSON object
        // defined in a raw resource file
        val success = map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
        )

    }

    private fun setMarker(latLng: LatLng, map: GoogleMap): Marker? {
        Log.d("select Fragment", "setMarker()")

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
        map.setOnMapLongClickListener {
            Log.d("select Fragment", "setMapLongClick()")


            mapMarker?.remove()
            // map.clear()

            mapMarker = setMarker(it, map)


        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener {
            Log.d("select Fragment", "setOnPoiClickListener()")
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
