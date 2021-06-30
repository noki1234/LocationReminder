package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
// Executes each task synchronously using Architecture Components.
@get:Rule
var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        )
        //Normally Room doesn't allow database queries to be run on the main thread.
        //Calling allowMainThreadQueries turns off this check. Don't do this in production code!
                .allowMainThreadQueries()
                .build()

        //Instantiate the TasksLocalDataSource, using your database and Dispatchers.Main. This will run your queries on the main thread (this is allowed because of allowMainThreadQueries).
        localDataSource =
                RemindersLocalRepository(
                        database.reminderDao(),
                        Dispatchers.Main
                )
    }

// TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun insertReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("title", "description", "location", 0.0, 1.0)
        localDataSource.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same reminder is returned.
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`("title"))
        assertThat(result.data.description, `is`("description"))
        assertThat(result.data.location, `is`("location"))
        assertThat(result.data.latitude, `is`(0.0))
        assertThat(result.data.longitude, `is`(1.0))
}

    @Test
    fun deleteReminders_retrievesNonExistingReminder() = runBlocking {
        // GIVEN
        val reminder = ReminderDTO("title", "description", "location", 0.0, 1.0)
        localDataSource.saveReminder(reminder)

        // WHEN  - Not existing reminder retrieved by ID.
        localDataSource.deleteAllReminders()
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Reminder not found
        assertThat(result.succeeded, `is`(false))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }




    @After
    fun cleanUp() {
        database.close()
    }


}