package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase


    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the process is killed.
        database = Room.inMemoryDatabaseBuilder(
                getApplicationContext(),
                RemindersDatabase::class.java
        ).build()
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("title", "description", "location", 0.0, 1.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))
        assertThat(loaded.latitude, `is`(reminder.latitude))
    }

    @Test
    fun insertAndDeleteAllRemindersAndCheckDbIsEmpty() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("title", "description", "location", 0.0, 1.0)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()

        // WHEN - Get the task by id from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(loaded.size, `is`(0))
    }

    @Test
    fun getUnexistingReminder() = runBlockingTest{
        val loaded: ReminderDTO? = database.reminderDao().getReminderById("0")
        assertThat<ReminderDTO>(loaded,  `is`(IsNull.nullValue()))
    }

    @After
    fun closeDb() = database.close()

}