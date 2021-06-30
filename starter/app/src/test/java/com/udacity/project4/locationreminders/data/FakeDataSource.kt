package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError = false
//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) return Result.Error("Reminders not found")
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error(
                Exception("Reminders not found").toString()
        )
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //WHEN
        if (shouldReturnError) return Result.Error("Reminders not found")
        reminders?.let {
            for (reminder in it) {
                if (reminder.id == id) {
                    return Result.Success(reminder)
                }
            }
        }
        return Result.Error(
                Exception("Reminder not found").toString()
        )
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


}