package org.ben.news.ui.sceptic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class ScepticViewModel : ViewModel() {
    private val scepticList =
        MutableLiveData<List<StoryModel>>()

    val observableScepticList: LiveData<List<StoryModel>>
        get() = scepticList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load(0) }

    private val outlet = "www.DailySceptic.org"


    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findByOutlet(list,outlet,scepticList)
            Timber.i("Load Success : ${scepticList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchByOutlet(dates, term, outlet, scepticList)
            Timber.i("Search Success")
        } catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}