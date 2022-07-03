package org.ben.news.ui.storyList

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

class StoryListViewModel : ViewModel() {

    private val storyList =
        MutableLiveData<List<StoryModel>>()

    val observableStoryList: LiveData<List<StoryModel>>
        get() = storyList

    private val story = MutableLiveData<StoryModel>()

    var observableStory: LiveData<StoryModel>
        get() = story
        set(value) {story.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    //var readOnly = MutableLiveData(false)

    init { load() }


    fun load() {
        var list = ArrayList<String>()
        list.add("07-01-22")
        try {
            val df = SimpleDateFormat("MM.dd.yy")

            val date = Calendar.getInstance().time
            val formattedDate: String = df.format(date)
            var newDate = formattedDate.replace(".","-")
            Timber.i("FORMATTED : $newDate")
            list.add(newDate)
            list.add("06-30-22")
            var yest = LocalDate.now()
            var yesterday = yest.minusDays(1)
            val year = yesterday.year.toString().substring(2)
            var month = yesterday.month.value.toString()
            if (month.length == 1){
                month = "0$month"
            }
            var day = yesterday.dayOfMonth.toString()
            if (day.length == 1){
                day = "0$day"
            }
            val yesterdaysDate = "$month-$day-$year"
            list.add(yesterdaysDate)


            Timber.i("LISTY : $list")
            StoryManager.findAll(list,storyList)
            Timber.i("Load Success : ${storyList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }



}