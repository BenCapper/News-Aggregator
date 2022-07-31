package org.ben.news.ui.revolver

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

class RevolverViewModel : ViewModel() {
    private val revList =
        MutableLiveData<List<StoryModel>>()

    val observableRevList: LiveData<List<StoryModel>>
        get() = revList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    private val outlet = "www.Revolver.news"
    private val df = SimpleDateFormat("MM.dd.yy")
    private val calDate = Calendar.getInstance().time
    private var formattedDate: String = df.format(calDate)
    private var today = formattedDate.replace(".","-")
    private val now = LocalDate.now()

    private fun getDates(n:Int): ArrayList<String> {
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
            StoryManager.findOutletNoImage(list,outlet,revList)
            Timber.i("Load Success : ${revList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( term: String) {
        val dates: ArrayList<String>
        try {
            dates = getDates(5)
            StoryManager.searchOutletNoImage(dates,term,outlet,revList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}