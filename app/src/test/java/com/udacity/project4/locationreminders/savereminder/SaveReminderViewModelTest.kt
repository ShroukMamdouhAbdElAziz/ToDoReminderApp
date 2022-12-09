package com.udacity.project4.locationreminders.savereminder


import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.testutils.MainCoroutineRule
import com.udacity.project4.testutils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.mockito.Mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
// unit test (local test) for SaveReminderViewModel and liveData
class SaveReminderViewModelTest {

    // execute every method in the test synchronously using Architecture components
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var applicationContext: Application

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var reminderFakeDataSource: FakeDataSource


    // get a fresh view model before every fun test
    @Before
    fun setupViewModel() {
        stopKoin()
        applicationContext = mock {
            on { getString(R.string.reminder_saved) } doReturn "Reminder Saved !"
        }

        reminderFakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(applicationContext, reminderFakeDataSource)
    }

    @Test
    fun insertReminder_saveReminder() {
        // GIVEN - fakeReminderDataItem
        val fakeReminderDataItem = ReminderDataItem(
            "reminder",
            "This is a fake reminder data item",
            "location",
            500.0,
            500.0,
            "reminderOne"
        )

        // WHEN - saving this item
        saveReminderViewModel.saveReminder(fakeReminderDataItem)

        // THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))

    }

    @Test
    fun validateReminder_emptyTitleReminder_returnFalse() {
        // GIVEN - fakeReminderDataItem with empty title
        val fakeReminderDataItem = ReminderDataItem(
            "",
            "This is a fake reminder data item",
            "location",
            500.0,
            500.0,
            "reminderOne"
        )
        // WHEN _ validating the data
        val result = saveReminderViewModel.isReminderValid(fakeReminderDataItem)
        // THEN returning false
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
        assertThat(result, `is`(false))


    }

    @Test
    fun validateReminder_emptyLocationReminder_returnFalse() {
        // GIVEN - fakeReminderDataItem with empty location
        val fakeReminderDataItem = ReminderDataItem(
            "reminder",
            "This is a fake reminder data item",
            "",
            500.0,
            500.0,
            "reminderOne"
        )
        // WHEN- validating the data
        val result = saveReminderViewModel.isReminderValid(fakeReminderDataItem)
        // THEN
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
        assertThat(result, `is`(false))


    }


}