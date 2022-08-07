package org.ben.news.ui.beast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class BeastViewModel : ViewModel() {
    private val beastList =
        MutableLiveData<List<StoryModel>>()

    val observableBeastList: LiveData<List<StoryModel>>
        get() = beastList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    private val outlet = "www.TheDailyBeast.com"


    fun load() {
        val list: ArrayList<String>
        try {
            list = StoryManager.getDates(5)
            list.sortDescending()
            StoryManager.findByOutlet(list,outlet,beastList)
            Timber.i("Load Success : ${beastList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( term: String) {
        val dates: ArrayList<String>
        try {
            dates = StoryManager.getDates(5)
            dates.sortDescending()
            StoryManager.searchByOutlet(dates,term,outlet,beastList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}