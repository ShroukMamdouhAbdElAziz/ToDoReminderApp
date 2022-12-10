package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
// to test the repository
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository


    @Before
    fun setUpDatabase() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Unconfined)
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        remindersDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertReminder_getRemindersByID() = runTest {

       //GIVEN  a reminder saved in the database
        val fakeReminderDto = ReminderDTO(
            "fakeReminder", "this is a fake one for test", "location", 500.0, 500.0, "reminderOne"
        )
        remindersLocalRepository.saveReminder(fakeReminderDto)

        // WHEN get reminder by ID
        val result = remindersLocalRepository.getReminder(fakeReminderDto.id)

        // THEN the result  is the fake data which inserted above
        assertThat(result, `is`(Result.Success(fakeReminderDto)))
    }

    @Test
    fun getNotFoundReminder_returnNull() = runTest {

        // GIVEN a reminder ID which doesn't exist in the database
        val reminderID = "reminderTwo"
        // When trying to retrieve this reminder item which doesn't exist
        val result = remindersLocalRepository.getReminder(reminderID)
        //THEN the result will be error msg Reminder not found!
        assertThat(result, `is`(Result.Error("Reminder not found!")))
    }


}