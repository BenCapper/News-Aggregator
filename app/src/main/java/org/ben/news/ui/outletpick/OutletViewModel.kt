package org.ben.news.ui.outletpick

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryModel
import timber.log.Timber

class OutletViewModel : ViewModel() {

    private val outletList =
        MutableLiveData<List<StoryModel>>()

    val observableOutletList: LiveData<List<StoryModel>>
        get() = outletList

    private val outlet = MutableLiveData<StoryModel>()

    var observableOutlet: LiveData<StoryModel>
        get() = outlet
        set(value) {outlet.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load(0) }

    fun load(day: Int) {
        val list: String
        try {
            list = StoryManager.getDate(day)
            StoryManager.findAll(list,outletList)
            Timber.i("Load Success : ${outletList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun search( day: Int, term: String) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.search(term,dates,outletList)
            Timber.i("Search Success")
        }
        catch (e: java.lang.Exception) {
            Timber.i("Search Error : $e.message")
        }
    }



    fun loadShuffle(day:Int) {
        try {
            val dates = StoryManager.getDate(day)
            StoryManager.findAllShuffle(dates,outletList)
            Timber.i("Load Success : ${outletList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

}