package org.ben.news.ui.dmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class DmailViewModel : ViewModel() {
    private val mailList =
        MutableLiveData<List<StoryModel>>()

    val observableMailList: LiveData<List<StoryModel>>
        get() = mailList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load(0) }

    private val outlet = "www.DailyMail.co.uk"


    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findByOutlet(list,outlet,mailList)
            Timber.i("Load Success : ${mailList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchByOutlet(dates, term, outlet, mailList)
            Timber.i("Search Success")
        } catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

}