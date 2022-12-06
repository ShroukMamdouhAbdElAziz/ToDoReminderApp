package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.monitorFragment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()




    private lateinit var applicationContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var reminderDataRepository: ReminderDataSource



    @Before
    fun init() {
        // Stop koin app.
        stopKoin()
        applicationContext = getApplicationContext()

        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    applicationContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(applicationContext) }
        }


        // start a new koin .
        startKoin {
            modules(listOf(myModule))
        }

        reminderDataRepository = get()

        //remove the previous reminders
        runBlocking {
            reminderDataRepository.deleteAllReminders()
        }
    }

   // @Before
  /*  fun initRepo() {
        reminderDataRepository = get()

        //remove the previous reminders
        runBlocking {
            reminderDataRepository.deleteAllReminders()
        }
    }*/


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }


    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun setInitialReminder_verifyData():Unit = runTest {
        // set initial reminder
        val reminder = ReminderDTO(
            "fake Reminder",
            "this is a fake reminder",
            "location1",
            500.0,
            500.0,
            "reminder1"
        )
        reminderDataRepository.saveReminder(reminder)

        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        // Click on the reminder and verify that all the data is correct.
        onView(ViewMatchers.withId(R.id.title))
            .check(ViewAssertions.matches(ViewMatchers.withText("fake Reminder")))
        onView(ViewMatchers.withId(R.id.description))
            .check(ViewAssertions.matches(ViewMatchers.withText("this is a fake reminder")))
        onView(ViewMatchers.withId(R.id.location))
            .check(ViewAssertions.matches(ViewMatchers.withText("location1")))

    }

    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {

        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN click on add button
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN verify navigation to saveFragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

}
