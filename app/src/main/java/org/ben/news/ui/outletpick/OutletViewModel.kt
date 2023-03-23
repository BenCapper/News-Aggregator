package org.ben.news.ui.outletpick

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import org.ben.news.firebase.StoryManager
import org.ben.news.models.OutletModel
import org.ben.news.models.StoryModel
import timber.log.Timber

class OutletViewModel : ViewModel() {

    private val outletList =
        MutableLiveData<List<OutletModel>>()

    val observableOutletList: LiveData<List<OutletModel>>
        get() = outletList

    private val outlet = MutableLiveData<OutletModel>()

    var observableOutlet: LiveData<OutletModel>
        get() = outlet
        set(value) {outlet.value = value.value}

    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    fun load() {
        try {
            // list = StoryManager.getDate()
            // StoryManager.findAll(list,outletList)
            val list = mutableListOf<OutletModel>()
            list.add(OutletModel("Gript", "ie"))
            list.add(OutletModel("RTE", "ie"))
            outletList.value = list
            Timber.i("Load Success : ${outletList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

}