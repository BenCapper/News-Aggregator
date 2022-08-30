package org.ben.news.ui.left

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class LeftViewModel : ViewModel() {
    private val leftList =
        MutableLiveData<List<StoryModel>>()

    val observableLeftList: LiveData<List<StoryModel>>
        get() = leftList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()


    init { load(0) }

    private val outlet = listOf("www.RTE.ie","news.Sky.com","www.TheGuardian.com","www.Euronews.com","AbcNews.go.com",
        "www.CbsNews.com","www.Vox.com","www.Politico.com","news.Yahoo.com","www.TheDailyBeast.com","www.HuffPost.com",
        "www.GlobalNews.ca")


    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findByOutlets(list,outlet,leftList)
            Timber.i("Load Success : ${leftList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchByOutlets(dates,term,outlet,leftList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

}