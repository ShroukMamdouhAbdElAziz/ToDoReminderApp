package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource as a test double for the LocalDataSource
// unit test
class FakeDataSource(private var reminderDto: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var isErrorOccured = true

    // used for testing error handling
    fun setErrorOccured(isErrorOccured: Boolean) {
        this.isErrorOccured = isErrorOccured

    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (isErrorOccured) {
            Result.Error("Error occurred while retrieving the Reminders")
        } else {
            Result.Success(reminderDto)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDto.add(reminder)
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {

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


    override suspend fun deleteAllReminders() {
        reminderDto.clear()
    }
}