package org.ben.news.ui.historyList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber


class HistoryListViewModel : ViewModel() {

    private val historyList =
        MutableLiveData<List<StoryModel>>()

    val observableHistoryList: LiveData<List<StoryModel>>
        get() = historyList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    fun load() {
        try {
            StoryManager.findLiked(liveFirebaseUser.value!!.uid,"history",historyList)
            Timber.i("Load Success : ${historyList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }

    }

    fun search(term: String) {
        try {
            StoryManager.searchLiked(term,liveFirebaseUser.value!!.uid,"history",historyList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

    fun delete(userid: String, id: String) {
        try {
            StoryManager.deleteLiked(userid,"history", id)
            Timber.i("Delete Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Delete Error : $e.message")
        }
    }
}