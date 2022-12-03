package com.udacity.project4.data

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

// use FakeDataSource to test ReminderLocalRepository
class FakeDataSource(var reminderDto:MutableList<ReminderDTO>?= mutableListOf()) :ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        TODO("Not yet implemented")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllReminders() {
        TODO("Not yet implemented")
    }
}