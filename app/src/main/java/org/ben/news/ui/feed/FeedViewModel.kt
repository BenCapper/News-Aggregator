package org.ben.news.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.OutletModel
import org.ben.news.models.StoryModel
import timber.log.Timber

class FeedViewModel : ViewModel() {

    private val feedList =
        MutableLiveData<List<StoryModel>>()

    private val outletList =
        MutableLiveData<List<String>>()

    val observableFeedList: LiveData<List<StoryModel>>
        get() = feedList

    val observableOutletList: LiveData<List<String>>
        get() = outletList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load(0) }

    private val outlet = "www.Revolver.news"


    fun load(day: Int) {
        var list: String
        val outlets = ArrayList<String>()
        try {
            StoryManager.findOutletLinks(liveFirebaseUser.value!!.uid, outletList) {
                // This block will be executed when the findOutletNames operation is complete
                Timber.i("NAMES FOUND VM  : ${outletList.value}")
                outletList.value?.let { outlets.addAll(it) }
                list = StoryManager.getDate(day)
                StoryManager.findByOutletFeed(list, outlets, feedList)
                Timber.i("NAMES FOUND VM2  : ${feedList.value}")
            }
        } catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.searchByOutlet(dates,term,outlet,feedList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

    fun loadShuffle(day:Int) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.findAllShuffle(dates,feedList)
            Timber.i("Load Success : ${feedList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

}