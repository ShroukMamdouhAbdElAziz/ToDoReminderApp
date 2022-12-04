package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.testutils.MainCoroutineRule
import com.udacity.project4.testutils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest{

    // execute every method in the test synchronously using Architecture components
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var reminderFakeDataSource: FakeDataSource


    // get fresh view model before every fun test
    @Before
    fun setupViewModel(){
        stopKoin()
      reminderFakeDataSource = FakeDataSource()
        remindersListViewModel= RemindersListViewModel(ApplicationProvider.getApplicationContext(),reminderFakeDataSource)
    }

    @Test
    fun loadReminders_shouldReturnError(){
        reminderFakeDataSource.setErrorOccured(true)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is` ("Error occurred while retrieving the Reminders"))

    }

}