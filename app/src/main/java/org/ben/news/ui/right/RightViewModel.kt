package org.ben.news.ui.right

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class RightViewModel : ViewModel() {
    private val rightList =
        MutableLiveData<List<StoryModel>>()

    val observableRightList: LiveData<List<StoryModel>>
        get() = rightList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()


    init { load(0) }

    private val outlet = listOf("www.BonginoReport.com","www.Gript.ie","www.GBNews.uk","www.Spiked-Online.com","www.ThePostMillennial.com",
        "www.TheBlaze.com","www.Timcast.com","www.Zerohedge.com","www.Breitbart.com","www.DailyCaller.com","www.TheGatewayPundit.com",
        "www.Revolver.news")


    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findByOutlets(list,outlet,rightList)
            Timber.i("Load Success : ${rightList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchByOutlets(dates,term,outlet,rightList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

}