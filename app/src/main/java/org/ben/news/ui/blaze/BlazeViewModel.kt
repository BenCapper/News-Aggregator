package org.ben.news.ui.blaze

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


class BlazeViewModel : ViewModel() {

    private val blazeList =
        MutableLiveData<List<StoryModel>>()

    val observableBlazeList: LiveData<List<StoryModel>>
        get() = blazeList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()


    init { load() }

    private val outlet = "www.TheBlaze.com"


    fun load() {
        val list: ArrayList<String>
        try {
            list = getDates(5)
            list.sortDescending()
            StoryManager.findByOutlet(list,outlet,blazeList)
            Timber.i("Load Success : ${blazeList.value}")
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
            StoryManager.searchByOutlet(dates,term,outlet,blazeList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}