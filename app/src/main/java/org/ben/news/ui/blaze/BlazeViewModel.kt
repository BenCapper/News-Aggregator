package org.ben.news.ui.blaze

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
    private val df = SimpleDateFormat("MM.dd.yy")
    private val calDate = Calendar.getInstance().time
    private var formattedDate: String = df.format(calDate)
    private var today = formattedDate.replace(".","-")
    private val now = LocalDate.now()

    private fun getDates(numDays:Int): ArrayList<String> {
        val dates = ArrayList<String>()
        for (i in 0..numDays) {
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
            StoryManager.searchByOutlet(dates,term,outlet,blazeList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }
}