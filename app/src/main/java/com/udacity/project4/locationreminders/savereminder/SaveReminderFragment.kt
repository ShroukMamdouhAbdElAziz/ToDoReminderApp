package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    lateinit var geofencingClient: GeofencingClient
    private var geofenceList = mutableListOf<Geofence>()

    // request both permissions(foreground and background ) separately

    private val foregroundLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                // enable location settings
                enableLocationSettingsConfiguration()
            } else {
                // show snackbar location denied
            }
        }


    private val backgroundLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // continue the app workflow
            } else {
                // shouldShowRequestPermissionRationale() dialog explain to the user
            }

        }


    // location setting configuration launcher
    private val locationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // enable location settings
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())

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
            val reminderDataItem = _viewModel.reminderDataItem.value
            /*  val title = _viewModel.reminderTitle.value
              val description = _viewModel.reminderDescription
              val location = _viewModel.reminderSelectedLocationStr.value
              val latitude = _viewModel.latitude
              val longitude = _viewModel.longitude.value*/

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
        }
    }

    fun createLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .setMaxUpdateDelayMillis(500)
            .setDurationMillis(1000)
            .setWaitForAccurateLocation(false)
            .build()

    }

    private fun enableLocationSettingsConfiguration() {
        val locationRequest = createLocationRequest()
        // get the current location
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        // then check whether the current location settings are satisfied
        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            if (it.locationSettingsStates?.isLocationUsable == true) {
                addGeofence()

            }else{1
               _viewModel.showSnackBarInt.value= R.string.reminder_not_saved
            }
        }
        //task.

    }


    private fun createGeofenceObject(reminder: ReminderDataItem): List<Geofence> {
        geofenceList.add(
            Geofence.Builder().setRequestId(reminder.id)
                .setCircularRegion(
                    reminder.latitude!!,
                    reminder.longitude!!, GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        )
        return geofenceList

    }

    private fun getGeofencingRequest(geofenceList: List<Geofence>): GeofencingRequest {

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()

    }

    private fun getPendingIntent(): PendingIntent {

        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        return pendingIntent
    }


    @SuppressLint("MissingPermission")
    private fun addGeofence() {
        var reminderDataItem = _viewModel.reminderDataItem.value
        if (reminderDataItem == null) {
            _viewModel.showSnackBarInt.value = R.string.select_location

        } else {
            _viewModel.saveReminder(reminderDataItem)
            val geofenceList = createGeofenceObject(reminderDataItem)
            val pendingIntent = getPendingIntent()
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), pendingIntent).run {
                  addOnSuccessListener {
                      _viewModel.showSnackBarInt.value =R.string.reminder_saved
                  }
                addOnFailureListener {
                    _viewModel.showSnackBarInt.value=R.string.geofences_not_added
                }
            }
        }
    }


    fun requestForegroundPermission() {
        foregroundLocationPermissionRequest.launch(FOREGROUND_LOCATION_PERMISSIONS)

    }

    // shouldShowRequestPermissionRationale() returns true if the app has requested the permission prev and
    //the user denied it and return false if the user turned down the permission req in the past
    // and choosed donot ask again
    fun shouldAskForegroundPermission() {

        val showRequestDialog =
            shouldShowRequestPermissionRationale(FOREGROUND_LOCATION_PERMISSIONS.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && showRequestDialog) {
            displayLocationPermissionsDialog()
        } else {
            requestForegroundPermission()
        }

    }


    private fun displayLocationPermissionsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(R.string.backgroundlocationpermission_rationale)
            .setPositiveButton("Accept") { _, _ ->
                requestBackgroundPermission()
            }
            .show()
    }


    fun requestBackgroundPermission() {
        backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    fun shouldAskBackgroundPermission() {
        val backgroundLocationPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(backgroundLocationPermission)
        ) {
            displayLocationPermissionsDialog()
        } else {
            requestBackgroundPermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        private val FOREGROUND_LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val GEOFENCE_RADIUS_IN_METERS = 1500f

    }
}
