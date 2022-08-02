package org.ben.news.ui.eu

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


class EuViewModel : ViewModel() {

    private val euList =
        MutableLiveData<List<StoryModel>>()

    val observableEuList: LiveData<List<StoryModel>>
        get() = euList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    private val outlets = listOf("www.Euronews.com")


    fun load() {
        val list: ArrayList<String>
        try {
            list = getDates(5)
            list.sortDescending()
            StoryManager.findByOutlets(list,outlets,euList)
            Timber.i("Load Success : ${euList.value}")
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
            StoryManager.searchByOutlets(dates,term,outlets,euList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}