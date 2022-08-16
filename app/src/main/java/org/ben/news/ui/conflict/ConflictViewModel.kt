package org.ben.news.ui.conflict

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.DoubleStoryModel
import org.ben.news.models.StoryModel
import timber.log.Timber

class ConflictViewModel : ViewModel() {
    private val conList =
        MutableLiveData<List<DoubleStoryModel>>()

    val observableConList: LiveData<List<DoubleStoryModel>>
        get() = conList

    private val story = MutableLiveData<DoubleStoryModel>()

    var observableStory: LiveData<DoubleStoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load(0) }

    private val outlets = listOf("www.Euronews.com")


    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findAllDouble(list,conList)
            Timber.i("Load Success : ${conList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchDouble(term,dates,conList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}