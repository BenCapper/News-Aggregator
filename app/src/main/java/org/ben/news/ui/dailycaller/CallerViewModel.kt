package org.ben.news.ui.dailycaller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
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
    private val df = SimpleDateFormat("MM.dd.yy")
    private val calDate = Calendar.getInstance().time
    private var formattedDate: String = df.format(calDate)
    private var today = formattedDate.replace(".","-")
    private val now = LocalDate.now()

    fun getDates(n:Int): ArrayList<String> {
        val dates = ArrayList<String>()
        for (i in 0..n) {
            val yesterday = now.minusDays(i.toLong())
            val year = yesterday.year.toString().substring(2)
            var month = yesterday.month.value.toString()
            if (month.length == 1) {
                month = "0$month"
            }
            var day = yesterday.dayOfMonth.toString()
            if (day.length == 1) {
                day = "0$day"
            }
            val date = "$month-$day-$year"
            dates.add(date)
        }
        return dates
    }

    fun load() {
        val list: ArrayList<String>
        try {
            list = getDates(5)
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
            StoryManager.searchByOutlet(dates,term,outlet,callerList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }

}