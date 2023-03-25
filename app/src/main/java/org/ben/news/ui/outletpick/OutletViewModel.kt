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
                list.add(OutletModel("ABC News", false, "AbcNews.go.com","us"))
                list.add(OutletModel("American Thinker", false, "www.AmericanThinker.com","us"))
                list.add(OutletModel("Bongino Report", false, "www.BonginoReport.com","us"))
                list.add(OutletModel("Breitbart", false, "www.Breitbart.com","us"))
                list.add(OutletModel("CBS News", false, "www.CbsNews.com","us"))
                list.add(OutletModel("Daily Mail", false, "www.DailyMail.co.uk","uk"))
                list.add(OutletModel("Euronews", false, "www.Euronews.com","eu"))
                list.add(OutletModel("GB News", false, "www.GBNews.uk","uk"))
                list.add(OutletModel("Global News", false, "www.GlobalNews.ca","ca"))
                list.add(OutletModel("Gript",false, "www.Gript.ie","ie"))
                list.add(OutletModel("Huffington Post", false, "www.HuffPost.com","us"))
                list.add(OutletModel("InfoWars", false, "www.InfoWars.com","us"))
                list.add(OutletModel("NPR", false, "www.Npr.org","us"))
                list.add(OutletModel("Politico", false, "www.Politico.com","us"))
                list.add(OutletModel("RTE News", false, "www.RTE.ie","ie"))
                list.add(OutletModel("Revolver News", false,"www.Revolver.news", "us"))
                list.add(OutletModel("Sky News", false, "news.Sky.com","uk"))
                list.add(OutletModel("Spiked Online", false, "www.Spiked-Online.com","uk"))
                list.add(OutletModel("The Blaze", false, "www.TheBlaze.com","us"))
                list.add(OutletModel("The Daily Beast", false, "www.TheDailyBeast.com","us"))
                list.add(OutletModel("The Daily Caller", false, "www.DailyCaller.com","us"))
                list.add(OutletModel("The Daily Sceptic", false, "www.DailySceptic.org","uk"))
                list.add(OutletModel("The Gateway Pundit", false, "www.TheGatewayPundit.com","us"))
                list.add(OutletModel("The Guardian", false, "www.TheGuardian.com","uk"))
                list.add(OutletModel("The Hill", false, "www.TheHill.com","us"))
                list.add(OutletModel("The Post Millennial", false, "www.ThePostMillennial.com","ca"))
                list.add(OutletModel("Timcast", false, "www.Timcast.com","us"))
                list.add(OutletModel("Trending Politics", false, "TrendingPoliticsNews.com","us"))
                list.add(OutletModel("Vox", false,"www.Vox.com", "us"))
                list.add(OutletModel("Yahoo News", false, "news.Yahoo.com","us"))
                list.add(OutletModel("Zerohedge", false, "www.Zerohedge.com","us"))
                outletList.value = list
                Timber.i("OUTLET LOAD 2 : ${outletList.value}")
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