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


    var liveFirebaseUser = MutableLiveData<FirebaseUser>()

    init { load() }

    fun load() {
        try {
            StoryManager.findOutlets(liveFirebaseUser.value!!.uid,outletList)
            Timber.i("Load Success : ${outletList.value}")
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
        try {
            if (outletList.value == null){
                val list = mutableListOf<OutletModel>()
                list.add(OutletModel("ABC", false, "us"))
                list.add(OutletModel("American Thinker", false, "us"))
                list.add(OutletModel("Blaze", false, "us"))
                list.add(OutletModel("Bongino Report", false, "us"))
                list.add(OutletModel("Breitbart", false, "us"))
                list.add(OutletModel("CBS", false, "us"))
                list.add(OutletModel("Daily Sceptic", false, "uk"))
                list.add(OutletModel("Euronews", false, "eu"))
                list.add(OutletModel("GB News", false, "uk"))
                list.add(OutletModel("Global News", false, "ca"))
                list.add(OutletModel("Gript",false, "ie"))
                list.add(OutletModel("Huffington Post", false, "us"))
                list.add(OutletModel("Infowars", false, "us"))
                list.add(OutletModel("NPR", false, "us"))
                list.add(OutletModel("Politico", false, "us"))
                list.add(OutletModel("RTE", false, "ie"))
                list.add(OutletModel("Revolver News", false, "us"))
                list.add(OutletModel("Sky News", false, "uk"))
                list.add(OutletModel("Spiked", false, "uk"))
                list.add(OutletModel("The Daily Beast", false, "us"))
                list.add(OutletModel("The Daily Caller", false, "us"))
                list.add(OutletModel("The Daily Mail", false, "uk"))
                list.add(OutletModel("The Gateway Pundit", false, "us"))
                list.add(OutletModel("The Guardian", false, "uk"))
                list.add(OutletModel("The Hill", false, "us"))
                list.add(OutletModel("The Post Millennial", false, "ca"))
                list.add(OutletModel("Timcast", false, "us"))
                list.add(OutletModel("Trending Politics", false, "us"))
                list.add(OutletModel("Vox", false, "us"))
                list.add(OutletModel("Yahoo News", false, "us"))
                list.add(OutletModel("Zerohedge", false, "us"))
                outletList.value = list
            }
        }
        catch (e: Exception) {
            Timber.i("Load Error : $e.message")
        }
    }

    fun saveOutlets(userId: String, outlets: List<OutletModel>){
        StoryManager.saveOutlets(userId, outlets)
    }

}