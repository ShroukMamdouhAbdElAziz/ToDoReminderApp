package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource as a test double for the LocalDataSource to be used in the testing so the class will implement ReminderDataSource
// unit test
// GIVEN the default data will be reminderDto which will be mutablelistof ReminderDTO
class FakeDataSource(private var reminderDto: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var isErrorOccured = true

    // used for testing error handling
    fun setErrorOccured(isErrorOccured: Boolean) {
        this.isErrorOccured = isErrorOccured

    }

    // simulate getReminders() in the ReminderLocalRepository which implement local data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        // WHEN (isErrorOccured) is true
        return if (isErrorOccured) {
            // THEN result will be an Error object with the error message
            Result.Error("Error occurred while retrieving the Reminders")
            // WHEN (isErrorOccured) is false
        } else {
            //THEN result will result the holds a Success with all the reminders in the list
            Result.Success(reminderDto)
        }
    }

    // simulate saveReminder() in the ReminderLocalRepository which implement local data source
    // GIVEN reminder: ReminderDTO
    // WHEN calling saveReminder() to add reminder to the list
    override suspend fun saveReminder(reminder: ReminderDTO) {
        // THEN add the reminder to the list
        reminderDto.add(reminder)
    }

    // simulate saveReminder() in the ReminderLocalRepository which implement local data source
    // GIVEN reminder: ReminderDTO
    // WHEN Get a reminder by its id ,id to be used to get the reminder
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
// THEN Result the holds a Success object with the Reminder or an Error object with the error message
        return if (!isErrorOccured) {
            var reminder = reminderDto.find {
                it.id == id
            }
            if (reminder != null) {
                Result.Success(reminder)
            } else {
                Result.Error("Reminder not found!")
            }
        } else {
            Result.Error("Error occurred")
        }
    }

    // simulate deleteAllReminders() in the ReminderLocalRepository which implement local data source
    // GIVEN the reminderDto list
    // WHEN calling deleteAllReminders
    override suspend fun deleteAllReminders() {
        // Then clear all the list
        reminderDto.clear()
    }
}