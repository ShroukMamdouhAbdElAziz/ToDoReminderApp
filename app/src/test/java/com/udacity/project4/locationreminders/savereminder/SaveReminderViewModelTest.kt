package com.udacity.project4.locationreminders.savereminder


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest{

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // execute every method in the test synchronously using Architecture components
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // get fresh view model before every fun test
    @Before
    fun setupViewModel(){
        saveReminderViewModel= SaveReminderViewModel(ApplicationProvider.getApplicationContext(),)
    }






}