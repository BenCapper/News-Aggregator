package org.ben.news.ui.spiked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class SpikedViewModel : ViewModel() {
    private val spikeList =
        MutableLiveData<List<StoryModel>>()

    val observableSpikeList: LiveData<List<StoryModel>>
        get() = spikeList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    private val outlet = "www.Spiked-Online.com"


    fun load() {
        val list: ArrayList<String>
        try {
            list = StoryManager.getDates(5)
            list.sortDescending()
            StoryManager.findByOutlet(list,outlet,spikeList)
            Timber.i("Load Success : ${spikeList.value}")
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
            StoryManager.searchByOutlet(dates,term,outlet,spikeList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}