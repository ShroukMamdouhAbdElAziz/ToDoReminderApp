package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.MatcherAssert.assertThat

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
// unit test (local test) to test DAO
class RemindersDaoTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setUpDatabase() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersDao = remindersDatabase.reminderDao()
    }


    @After
    fun closeDatabase() {
        remindersDatabase.close()
    }


    @Test
    fun insertReminder_getRemindersByID() = runTest {
        //GIVEN  a reminder saved in the database
        val fakeReminderDto = ReminderDTO(
            "fakeReminder", "this is a fake one for test", "location", 500.0, 500.0, "reminderOne"
        )

        remindersDao.saveReminder(fakeReminderDto)

        // WHEN get reminder by ID from the database
        val reminder = remindersDao.getReminderById(fakeReminderDto.id)

        // THEN same reminder details are retrieved
        assertThat(reminder, notNullValue())
        assertThat(reminder?.title, `is`(fakeReminderDto.title))
        assertThat(reminder?.description, `is`(fakeReminderDto.description))
        assertThat(reminder?.location, `is`(fakeReminderDto.location))
        assertThat(reminder?.latitude, `is`(fakeReminderDto.latitude))
        assertThat(reminder?.longitude, `is`(fakeReminderDto.longitude))
    }


    @Test
    fun unExistedReminder_returnNull() = runTest {
        // GIVEN a reminder ID which doesn't exist in the database
        val reminderID = "reminderTwo"
        // When trying to retrieve this reminder item which doesn't exist
        val expectedReminder = remindersDao.getReminderById(reminderID)
        //THEN the expected value isa null
        assertThat(expectedReminder, nullValue())
    }


}