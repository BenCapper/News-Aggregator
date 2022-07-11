package org.ben.news.ui.likedList

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

class LikedListViewModel : ViewModel() {
    private val likedList =
        MutableLiveData<List<StoryModel>>()

    val observableLikedList: LiveData<List<StoryModel>>
        get() = likedList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    //var readOnly = MutableLiveData(false)

    init { load() }

    private val df = SimpleDateFormat("MM.dd.yy")
    private val calDate = Calendar.getInstance().time
    private var formattedDate: String = df.format(calDate)
    private var today = formattedDate.replace(".","-")
    private val now = LocalDate.now()

    fun getDates(n:Int): ArrayList<String> {
        var dates = ArrayList<String>()
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
        var list: ArrayList<String>
        try {
            list = getDates(5)
            Timber.i("LISTY : $list")
            StoryManager.findLiked(liveFirebaseUser.value!!.uid,likedList)
            Timber.i("Load Success : ${likedList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }

    }

    fun search( term: String) {
        try {
            StoryManager.searchLiked(term,liveFirebaseUser.value!!.uid,likedList)
            Timber.i("Building Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Building Search Error : $e.message")
        }
    }

}