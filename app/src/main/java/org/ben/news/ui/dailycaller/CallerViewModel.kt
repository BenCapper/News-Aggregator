package org.ben.news.ui.dailycaller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.firebase.StoryManager.getDates
import org.ben.news.models.StoryModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class CallerViewModel : ViewModel() {

    private val callerList =
        MutableLiveData<List<StoryModel>>()

    val observableCallerList: LiveData<List<StoryModel>>
        get() = callerList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    private val outlet = "www.DailyCaller.com"


    fun load() {
        val list: ArrayList<String>
        try {
            list = getDates(5)
            list.sortDescending()
            StoryManager.findByOutlet(list,outlet,callerList)
            Timber.i("Load Success : ${callerList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }

    }

    fun search( term: String) {
        val dates: ArrayList<String>
        try {
            dates = getDates(5)
            dates.sortDescending()
            StoryManager.searchByOutlet(dates,term,outlet,callerList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

}