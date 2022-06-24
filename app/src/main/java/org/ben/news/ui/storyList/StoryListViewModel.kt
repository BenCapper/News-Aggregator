package org.ben.news.ui.storyList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class StoryListViewModel : ViewModel() {

    private val storyList =
        MutableLiveData<List<StoryModel>>()

    val observableStoryList: LiveData<List<StoryModel>>
        get() = storyList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    //var readOnly = MutableLiveData(false)

    init { load() }


    fun load() {
        try {
            val c: Date = Calendar.getInstance().time
            val df = SimpleDateFormat("MM.dd.yy")
            val formattedDate: String = df.format(c)
            var newDate = formattedDate.replace(".","-")
            Timber.i("FORMATTED : $newDate")
            StoryManager.findAllByDateOutlet(newDate, "Timcast",storyList)
            Timber.i("Load Success : ${storyList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }


    fun loadAll() {
        try {
            val c: Date = Calendar.getInstance().time
            val df = SimpleDateFormat("MM.dd.yy")
            val formattedDate: String = df.format(c)
            var newDate = formattedDate.replace(".","-")
            Timber.i("FORMATTED : $newDate")
            StoryManager.findAllByDateOutlet(newDate, "Timcast",storyList)
            Timber.i("Load Success : ${storyList.value.toString()}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }


}