package com.udacity.project4


import android.Manifest
import android.app.Application
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.DataBindingIdlingResource
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.R.*
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.coroutines.delay
import org.junit.Rule
import org.hamcrest.Matchers.not


@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var applicationContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var reminderDataRepository: ReminderDataSource
    private lateinit var decorView: View


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

    // starting the test from the beginning without any reminders items existing
    @Test
    fun testReminderActivity_setInitialReminder() {
        // Start up Tasks screen.
        launchActivity<RemindersActivity>().use { scenario ->
            dataBindingIdlingResource.monitorActivity(scenario)

            scenario.onActivity { activity ->
                decorView = activity.window.decorView
            }


            // starting open  the screen for first time without any data existing
            onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            // click on add button  - reminderlistfrag
            onView(withId(R.id.addReminderFAB)).perform(click())

            // write the details
            onView(withId(R.id.reminderTitle))
                .perform(typeText("reminderOne"))

            onView(withId(R.id.reminderDescription)).perform(typeText(" Go to Gym"))

            // click on selectlocation button
            onView(withId(R.id.selectLocation)).perform(click())


            // set the location on map
            onView(withId(R.id.map)).perform(longClick())
            // save the selected location on map (save btn on map)
            onView(withId(R.id.save_button)).perform(click())

            // save the item
            onView(withId(R.id.saveReminder)).perform(click())
            // show the toast once the save is done
            onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(decorView))).check(
                matches(isDisplayed())
            )


            // validate the data
            onView(withId(R.id.title)).check(matches(withText("reminderOne")))
            onView(withId(R.id.description)).check(matches(withText(" Go to Gym")))


        }

    }

    @Test
    fun testSnackBar_noTitleEntered() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderDescription)).perform(
            typeText(" Go to school")
        )

        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())
        runBlocking {
            delay(2000)

        }
        onView(withId(id.snackbar_text)).check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun testSnackBar_nolocationSelected() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle))
            .perform(typeText("reminderOne"))

        onView(withId(R.id.reminderDescription)).perform(
            typeText(" Go to school")
        )

        Espresso.closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withId(id.snackbar_text)).check(matches(withText(R.string.err_select_location)))

        activityScenario.close()
    }
}
