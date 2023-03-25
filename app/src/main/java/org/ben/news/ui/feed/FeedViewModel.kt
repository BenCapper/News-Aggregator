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
        MutableLiveData<List<OutletModel>>()

    val observableFeedList: LiveData<List<StoryModel>>
        get() = feedList

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load(0) }

    private val outlet = "www.Euronews.com"

    fun getOutlets(){
        try {
            StoryManager.findOutlets(liveFirebaseUser.value!!.uid,outletList)
        }
        catch (e: Exception) {
            Timber.i("GET OUTLETS ERROR : $e.message")
        }
    }

    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findByOutlet(list,outlet,feedList)
            Timber.i("Load Success : ${feedList.value}")
        }
        catch (e: Exception) {
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