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
class RemindersLocalRepositoryTest {

    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


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
    fun insertReminder_getRemindersByID() = runBlocking {

        // GIVEN
        val fakeReminderDto = ReminderDTO(
            "fakeReminder", "this is a fake one for test", "location", 500.0, 500.0, "reminderOne"
        )
        remindersLocalRepository.saveReminder(fakeReminderDto)

        // WHEN get reminder by ID
        val result = remindersLocalRepository.getReminder(fakeReminderDto.id)

        // THEN.
        assertThat(result, `is`(Result.Success(fakeReminderDto)))
    }

    @Test
    @Throws(Exception::class)
    fun unSavedReminder_returnNull() = runTest {

        // GIVEN
        val reminderID = "reminderTwo"
        // When
        val result = remindersLocalRepository.getReminder(reminderID)

        assertThat(result, `is`(Result.Error("data not found")))
    }


}