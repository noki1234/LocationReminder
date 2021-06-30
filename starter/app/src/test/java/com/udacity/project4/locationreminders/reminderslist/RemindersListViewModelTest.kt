package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var context: Application
    private lateinit var remindersList: MutableList<ReminderDTO>

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        context = ApplicationProvider.getApplicationContext()
        remindersList = mutableListOf(
            ReminderDTO(
                title = "Aupark",
                description = "Shopping mall",
                location = "location",
                latitude = 48.7178,
                longitude = 21.2641,
                id = "0"
            ),
            ReminderDTO(
                title = "Mc Donalnd",
                description = "Food yummmy",
                location = "location",
                latitude = 48.71961,
                longitude = 21.26520,
                id = "1"
            ),
            ReminderDTO(
                title = "Church",
                description = "Amen",
                location = "location",
                latitude =  48.72043,
                longitude = 21.25827,
                id = "2"
            )
        )
        dataSource = FakeDataSource(remindersList)
        remindersListViewModel = RemindersListViewModel(context , dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun checkListNotEmpty() {
        remindersListViewModel.loadReminders()
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        Assert.assertThat(reminders.isEmpty(), Matchers.`is`(false))
    }

    @Test
    fun checkListEmpty() {
        val emptyDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(context, emptyDataSource)
        remindersListViewModel.loadReminders()
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        Assert.assertThat(reminders.isEmpty(), Matchers.`is`(true))
    }

    @Test
    fun checkLoading() = mainCoroutineRule.runBlockingTest {
            mainCoroutineRule.pauseDispatcher()
            remindersListViewModel.loadReminders()
            Assert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(true))
            mainCoroutineRule.resumeDispatcher()
            Assert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false))
    }


    @Test
    fun checkErrorRemindersNotFound() {
        val emptyDataSource = FakeDataSource()
        emptyDataSource.setReturnError(true)
        remindersListViewModel = RemindersListViewModel(context, emptyDataSource)
        remindersListViewModel.loadReminders()
        Assert.assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Reminders not found"))
    }
}