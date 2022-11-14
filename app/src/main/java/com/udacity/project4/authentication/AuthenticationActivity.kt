package com.udacity.project4.authentication


import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.location.GeofencingClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    lateinit var binding: ActivityAuthenticationBinding



    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result: FirebaseAuthUIAuthenticationResult? ->
            val response = result!!.idpResponse

            if (result.resultCode === RESULT_OK) {
                // Successfully signed in
                startActivity(Intent(this, RemindersActivity::class.java))
                finish()
                Log.d(TAG,"successful login")

            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar("Login is failed")

                }

                if (response!!.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    showSnackbar("no internet connection")

                }

                showSnackbar("unknown error occured while trying to login")
                Log.e(TAG, "Sign-in error: ", response.error)
            }


        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityAuthenticationBinding.inflate(layoutInflater)
        val view = binding.root

        val auth = FirebaseAuth.getInstance()

        //  TODO: If the user was authenticated, send him to RemindersActivity
        if (auth.currentUser != null) {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)

        } else {
            //  TODO: If the user wasn't authenticated before, send him to Login
            setContentView(view)
            startLogin()

        }

    }





    private val providers = listOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )


    //TODO: Implement the create account and sign in using FirebaseUI,
    // use sign in using email and sign in using Google
    private fun startLogin() {

        binding.loginBtn.setOnClickListener {
            signInLauncher.launch(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)

                    //TODO: a bonus is to customize the sign in flow to look nice using :
                    //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
                    .setTheme(R.style.AppTheme)
                    .setLogo(R.drawable.map)
                    .build()
            )

        }
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }
}



