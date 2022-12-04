package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import org.hamcrest.MatcherAssert.assertThat

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun setUpDatabase() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersDao = remindersDatabase.reminderDao()
    }


    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        remindersDatabase.close()
    }


    @Test
    @Throws(Exception::class)
    fun insertReminder_getRemindersByID() = runTest {
        val fakeReminderDto = ReminderDTO(
            "fakeReminder", "this is a fake one for test", "location", 500.0, 500.0, "reminderOne"
        )
        // GIVEN insert reminder
        remindersDao.saveReminder(fakeReminderDto)


        // WHEN get reminder by ID
        val reminder = remindersDao.getReminderById(fakeReminderDto.id)

        // THEN
        assertThat(reminder, notNullValue())
        assertThat(reminder?.title, `is`(fakeReminderDto.title))
        assertThat(reminder?.description, `is`(fakeReminderDto.description))
        assertThat(reminder?.location, `is`(fakeReminderDto.location))
        assertThat(reminder?.latitude, `is`(fakeReminderDto.latitude))
        assertThat(reminder?.longitude, `is`(fakeReminderDto.longitude))
    }


    @Test
    @Throws(Exception::class)
    fun unSavedReminder_returnNull() = runTest {
        val reminderID = "reminderTwo"
        val expectedReminder = remindersDao.getReminderById(reminderID)

        assertThat(expectedReminder, nullValue())
    }



}