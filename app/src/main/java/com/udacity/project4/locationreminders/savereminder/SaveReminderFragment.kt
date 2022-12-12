package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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

            Log.d("saveFragment", "handling savebtn")
            requestLocationPermissions()
        }

    }

    private fun requestForegroundLocationPermissions() {
        Log.d("saveFragment", " requestFgPermissions()")
        val shouldShowRequestRationale =
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestRationale) {
            displayLocationPermissionsDialog()
            Log.d("saveFragment", "after displayLocationDialog")
        } else {
            foregroundLocationPermissionRequest.launch(FOREGROUND_LOCATION_PERMISSIONS)
            Log.d("saveFragment", "launchFgPermission")
        }
    }

    private fun displayLocationPermissionsDialog() {
        Log.d("saveFragment", "enterDiplaydialogFun")
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.location_required_error)
            .setMessage(R.string.backgroundlocationpermission_rationale)
            .setPositiveButton("Accept") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    Log.d("saveFragment", "enterDiplaydialogFun Bg")
                } else {
                    foregroundLocationPermissionRequest.launch(FOREGROUND_LOCATION_PERMISSIONS)
                    Log.d("saveFragment", "enterDiplaydialogFun Fg")
                }

            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        Log.d("saveFragment", "requestBackgroundLocationPermission()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {

            displayLocationPermissionsDialog()
            Log.d(
                "saveFragment",
                "requestBackgroundLocationPermission()/displayLocationPermissionsDialog"
            )
        } else {
            backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            Log.d(
                "saveFragment",
                "requestBackgroundLocationPermission()/backgroundLocationPermissionRequest"
            )
        }
    }

    private fun requestLocationPermissions() {
        Log.d("savefrag", "requestLocationPermissions")
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
        Log.d("savefrag", "createLocationRequest()")
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

    }

    private fun enableLocationSettingsConfiguration() {
        Log.d("savefrag", "enableLocationSettingsConfiguration()")
        val locationRequest = createLocationRequest()
        // get the current location
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        // then check whether the current location settings are satisfied
        val client = LocationServices.getSettingsClient(requireContext())
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            if (it.locationSettingsStates?.isLocationUsable == true) {
                saveReminderItem()
            } else {
                _viewModel.showSnackBarInt.value = R.string.reminder_not_saved
            }
        }
        task.addOnFailureListener {
            if (it is ResolvableApiException) {

                locationRequestLauncher.launch(
                    IntentSenderRequest.Builder(it.resolution.intentSender).build()
                )

            } else {
                _viewModel.showSnackBarInt.value = R.string.reminder_not_saved
            }
        }

    }

    private fun saveReminderItem() {
        Log.d("saveFrag", "saveReminderItem()")
        val reminderDataItem = _viewModel.reminderDataItem.value
        if (reminderDataItem != null
            && _viewModel.isReminderValid(reminderDataItem)
        ) {
            _viewModel.saveReminder(reminderDataItem)
            addGeofence()
        }

    }


    private fun createGeofenceObject(reminder: ReminderDataItem): List<Geofence> {
        Log.d("saveFrag", "createGeofenceObject()")
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
        Log.d("saveFrag", "getGeofencingRequest()")

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()

    }

    private fun getPendingIntent(): PendingIntent {
        Log.d("saveFrag", "getPendingIntent()")
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


    @SuppressLint("MissingPermission", "SuspiciousIndentation")
    private fun addGeofence() {
        Log.d("saveFrag", "addGeofence()")
        val reminderDataItem = _viewModel.reminderDataItem.value!!

        _viewModel.saveReminder(reminderDataItem)
        val geofenceList = createGeofenceObject(reminderDataItem)
        val pendingIntent = getPendingIntent()
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), pendingIntent).run {
            addOnSuccessListener {
                Log.d("saveFrag", "GeofenceAdded")
                _viewModel.showSnackBarInt.value = R.string.reminder_saved
            }
            addOnFailureListener {
                Log.d("saveFrag", "GeofenceNotAdded")
                _viewModel.showSnackBarInt.value = R.string.geofences_not_added
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
