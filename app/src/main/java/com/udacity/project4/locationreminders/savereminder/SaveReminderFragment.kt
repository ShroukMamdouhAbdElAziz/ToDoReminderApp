package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
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
                // no location access granted
                _viewModel.showSnackBarInt.value = R.string.no_location_access_granted

            }
        }


    private val backgroundLocationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // continue the app workflow
                enableLocationSettingsConfiguration()
            } else {
                _viewModel.showSnackBarInt.value = R.string.no_location_access_granted
            }

        }


    // location setting configuration launcher
    private val locationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // enable location settings
            enableLocationSettingsConfiguration()
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

            // TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            requestLocationPermissions()
            val reminderDataItem = _viewModel.reminderDataItem.value
            if (reminderDataItem != null) {
                _viewModel.validateAndSaveReminder(reminderDataItem)
            }

        }
    }

    private fun requestForegroundLocationPermissions() {
        val shouldShowRequestRationale =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestRationale) {
            displayLocationPermissionsDialog()
        } else {
            foregroundLocationPermissionRequest.launch(FOREGROUND_LOCATION_PERMISSIONS)
        }
    }

    private fun displayLocationPermissionsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(R.string.backgroundlocationpermission_rationale)
            .setPositiveButton("Accept") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    foregroundLocationPermissionRequest.launch(FOREGROUND_LOCATION_PERMISSIONS)
                }

            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            displayLocationPermissionsDialog()
        } else {
            backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private fun requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBackgroundLocationPermission()
            } else {
                enableLocationSettingsConfiguration()
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestForegroundLocationPermissions()
            } else {
                enableLocationSettingsConfiguration()
            }
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

            } else {
                1
                _viewModel.showSnackBarInt.value = R.string.reminder_not_saved
            }
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {
                try {
                    locationRequestLauncher.launch(
                        IntentSenderRequest.Builder(it.resolution.intentSender).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            } else {
                _viewModel.showSnackBarInt.value = R.string.reminder_not_saved
            }
        }

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
                    _viewModel.showSnackBarInt.value = R.string.reminder_saved
                }
                addOnFailureListener {
                    _viewModel.showSnackBarInt.value = R.string.geofences_not_added
                }
            }
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
        private const val GEOFENCE_RADIUS_IN_METERS = 500f

    }
}
