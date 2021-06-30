package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private val poi = PointOfInterest(LatLng(1.0, 0.0), "PlaceId", "Title")

    //TODO: provide testing to the SaveReminderView and its live data objects

    @Before
    fun setUpViewModel(){
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())

        saveReminderViewModel.reminderTitle.value = "Title"
        saveReminderViewModel.reminderDescription.value = "Description"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Location"
        saveReminderViewModel.latitude.value = 1.0
        saveReminderViewModel.longitude.value = 0.0
        saveReminderViewModel.selectedPOI.value = poi

    }

    @Test
    fun validateCorrectData() {
        // Given a fresh SaveReminderViewModel
        val title = saveReminderViewModel.reminderTitle.getOrAwaitValue()
        assertThat(title, `is`("Title"))
        val description = saveReminderViewModel.reminderDescription.getOrAwaitValue()
        assertThat(description, `is`("Description"))
        val location = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        assertThat(location, `is`("Location"))
        val latitude = saveReminderViewModel.latitude.getOrAwaitValue()
        assertThat(latitude, `is`(1.0))
        val longitude = saveReminderViewModel.longitude.getOrAwaitValue()
        assertThat(longitude, `is`(0.0))
        val poi = saveReminderViewModel.selectedPOI.getOrAwaitValue()
        assertThat(poi, `is`(poi))

        val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
        //When adding a new task
        val validated = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validated, `is`(true))
    }

    @Test
    fun validateIncorrectData(){
        saveReminderViewModel.onClear()

        val title = saveReminderViewModel.reminderTitle.getOrAwaitValue()
        val description = saveReminderViewModel.reminderDescription.getOrAwaitValue()
        val location = saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val latitude = saveReminderViewModel.latitude.getOrAwaitValue()
        val longitude = saveReminderViewModel.longitude.getOrAwaitValue()

        val reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)

        val validated = saveReminderViewModel.validateEnteredData(reminderDataItem)
        assertThat(validated, `is`(false))
    }

    @Test
    fun checkLoading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        val reminderDataItem = ReminderDataItem("title", "description", "location", 1.0, 0.0)
        saveReminderViewModel.saveReminder(reminderDataItem)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @After
    fun tearDown() {
        stopKoin()
    }

}