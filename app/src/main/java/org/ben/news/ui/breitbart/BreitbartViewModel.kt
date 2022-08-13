package org.ben.news.ui.breitbart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber


class BreitbartViewModel : ViewModel() {

    private val breitList =
        MutableLiveData<List<StoryModel>>()

    val observableBreitList: LiveData<List<StoryModel>>
        get() = breitList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    //var readOnly = MutableLiveData(false)

    init { load(0) }

    private val outlet = "www.Breitbart.com"


    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findByOutlet(list,outlet,breitList)
            Timber.i("Load Success : ${breitList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchByOutlet(dates,term,outlet,breitList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }


}