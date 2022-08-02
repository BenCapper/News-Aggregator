package org.ben.news.ui.us

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


class UsViewModel : ViewModel() {

    private val usList =
        MutableLiveData<List<StoryModel>>()

    val observableUsList: LiveData<List<StoryModel>>
        get() = usList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    private val outlets = listOf("www.TheBlaze.com",
        "www.Timcast.com",
        "www.Zerohedge.com",
        "www.Breitbart.com",
        "www.Revolver.news",
        "www.DailyCaller.com",
        "www.TheGatewayPundit.com",
        "www.Politico.com",
        "www.CbsNews.com",
        "AbcNews.go.com",
        "news.Yahoo.com",
        "www.Vox.com")


    fun load() {
        val list: ArrayList<String>
        try {
            list = getDates(5)
            list.sortDescending()
            StoryManager.findByOutlets(list,outlets,usList)
            Timber.i("Load Success : ${usList.value}")
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
            StoryManager.searchByOutlets(dates,term,outlets,usList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}