package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: FragmentSelectLocationBinding
    // Geofence
    lateinit var geofencingClient:GeofencingClient

    private var mapMarker: Marker? = null
    private val TAG = SelectLocationFragment::class.java.simpleName

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()


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

        // Geofence
        geofencingClient=LocationServices.getGeofencingClient(requireActivity())

        setHasOptionsMenu(true)

        setDisplayHomeAsUpEnabled(true)






//        TODO: call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }


    // onMapReady() is called when thee map is loaded
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


        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()

    }

    //to add marker when the user touches and holds on the map to place a marker at a location on the map
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapClickListener {

            // remove the old marker
            mapMarker?.remove()

            mapMarker = setMarker(it,map)
        }
    }
    private fun setMarker(latLng: LatLng,map: GoogleMap): Marker? {
        // to diplay the coordinates of selected location
        val snippet = String.format(
            Locale.getDefault(), "Lat: %1$.5f , Long: %2$.5f", latLng.latitude,latLng.longitude
        )
        var markerOptions=MarkerOptions().position(latLng)
            .title(getString(R.string.dropped_pin)).snippet(snippet)
            // to customize the color of marker for long pressed
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        return map.addMarker(markerOptions)
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

    private fun setMapStyle(map: GoogleMap) {
        try {
            // customize the styling of the base map using a JSON object
            // defined in a raw resource file
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
    // To check if the user has granted the location permission
    // return true if permission is granted and false if not granted
    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    }


    // to enable the location layer to track the user current location only for requesting the permission
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {

            // Set  mylocationenable to true
            map.isMyLocationEnabled = true
            createLocationRequest()
        } else {
            //if permission is not granted , request a user permission
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_Code
            )
        }
    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()
        // get the current Location
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    // This callback is the result from requesting permissions after the user enables the permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        //check if the location permissions are granted and if so enable
        //the location data layer
        if (requestCode == LOCATION_PERMISSION_REQUEST_Code) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                createLocationRequest()
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_Code = 1
        private const val REQUEST_CHECK_SETTINGS = 1000
    }

    private fun onLocationSelected() {
        _viewModel.showSnackBar.value = "Kind"
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    // for geofence
    private fun addCircle(latLng: LatLng,radius:Float) {
         var circleOptions = CircleOptions()
        circleOptions.also {
            it.center(latLng)
                .radius(radius.toDouble())
                .strokeColor(Color.argb(255,255,0,0))
                .fillColor(Color.argb(64,255,0,0))
                .strokeWidth(4F)
            map.addCircle(circleOptions)
        }

    }



}
