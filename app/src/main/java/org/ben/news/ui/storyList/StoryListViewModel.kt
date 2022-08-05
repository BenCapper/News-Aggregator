package org.ben.news.ui.storyList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.firebase.StoryManager.getDates
import org.ben.news.models.StoryModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


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

    init { load() }


    fun load() {
        val list: ArrayList<String>
        try {
            list = getDates(1)
            list.sortDescending()
            StoryManager.findAll(list,storyList)
            Timber.i("Load Success : ${storyList.value}")
        }
        catch (e: Exception) {
                Timber.i("Load Error : $e.message")
        }
    }

    fun loadShuffle() {
        val list: ArrayList<String>
        try {
            list = getDates(1)
            list.sortDescending()
            StoryManager.findAllShuffle(list,storyList)
            Timber.i("Load Success : ${storyList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( term: String) {
        val dates: ArrayList<String>
        try {
            dates = getDates(1)
            dates.sortDescending()
            StoryManager.search(term,dates,storyList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}