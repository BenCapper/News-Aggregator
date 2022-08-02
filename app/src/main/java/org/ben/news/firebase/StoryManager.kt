package org.ben.news.firebase


import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import org.ben.news.models.StoryModel
import org.ben.news.models.StoryStore
import timber.log.Timber
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt
import kotlin.random.nextInt


object StoryManager : StoryStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getDates(n:Int): ArrayList<String> {
        val now = LocalDate.now()
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
    private fun formatTitle(title: String): String {
        return title.replace("(dot)", ".")
            .replace("(pc)", "%")
            .replace("(plus)", "+")
            .replace("(colon)", ":")
            .replace("(hash)", "#")
            .replace("(quest)", "?")
            .replace("(comma)", ",")
            .replace("(USD)", "$")
    }

    private fun formatTitleIllegal(title: String): String {
        return title.replace(".", "(dot)")
            .replace("%", "(pc)")
            .replace("(plus)", "+")
            .replace(":", "(colon)")
            .replace("#", "(hash)")
            .replace("?", "(quest)")
            .replace(",", "(comma)")
            .replace("$", "(USD)")
            .replace("&amp;", "and")
    }

    override fun findAll(dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>) {

            val totalList = ArrayList<StoryModel>()
            for (date in dates) {
                var todayList = mutableListOf<StoryModel>()
                database.child("stories").child(date)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            Timber.i("Firebase Timcast error : ${error.message}")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            children.forEach {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                            todayList = todayList.sortedBy{it.order}.toMutableList()
                            database.child("stories").child(date)
                                .removeEventListener(this)
                            totalList.addAll(todayList)
                            storyList.value = totalList

                        }
                    })
        }
    }

    override fun search(term: String, dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase building error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) ||
                                it.getValue(StoryModel::class.java)?.outlet!!.contains(term, true) ||
                                it.getValue(StoryModel::class.java)?.date!!.contains(term, true)
                            ) {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun findByOutlet(dates: ArrayList<String>, outlet: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            if(story?.outlet == outlet) {
                                story.title = formatTitle(story.title)
                                todayList.add(story)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList

                    }
                })
        }
    }

    override fun findByOutlets(dates: ArrayList<String>, outlets: List<String>, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            if(story?.outlet in outlets) {
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList

                    }
                })
        }
    }

    override fun findOutletNoImage(dates: ArrayList<String>, outlet: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child("Found on: $date")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            if(story?.outlet == outlet) {
                                story.title = formatTitle(story.title)
                                todayList.add(story)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child("Found on: $date")
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList

                    }
                })
        }
    }


    override fun find(userId: String, path:String, dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
            database.child("user-$path").child(userId).child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                            totalList.add(story!!)
                            Timber.i("user-article=$story")
                        }
                        database.child("user-$path").child(userId).child(date)
                            .removeEventListener(this)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun search(term: String, userId: String, path:String, dates: ArrayList<String>,  storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        for(date in dates) {
            database.child("user-$path").child(userId).child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) ||
                                it.getValue(StoryModel::class.java)?.outlet!!.contains(
                                    term,
                                    true
                                ) ||
                                it.getValue(StoryModel::class.java)?.date!!.contains(term, true)
                            ) {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title =
                                    story?.title?.let { it -> formatTitle(it) }.toString()
                                totalList.add(story!!)
                            }
                        }

                        database.child("user-$path").child(userId).child(date)
                            .removeEventListener(this)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun searchByOutlet(dates: ArrayList<String>, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) &&
                                it.getValue(StoryModel::class.java)?.outlet!! == outlet
                            ) {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun searchByOutlets(dates: ArrayList<String>, term: String, outlets:List<String>, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) &&
                                it.getValue(StoryModel::class.java)?.outlet!! in outlets
                            ) {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun searchOutletNoImage(dates: ArrayList<String>, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child("Found on: $date")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) &&
                                it.getValue(StoryModel::class.java)?.outlet!! == outlet
                            ) {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                        }
                        todayList = todayList.sortedBy{it.storage_link}.toMutableList()
                        database.child("stories").child("Found on: $date")
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun findById(userId: String, storyId: String, story: MutableLiveData<StoryModel>) {

        database.child("user-stories").child(userId)
            .child(storyId).get().addOnSuccessListener {
                story.value = it.getValue(StoryModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }
    }

    override fun create(userId: String, path:String, story: StoryModel) {
        val storyValues = story.toMap()
        val childAdd = HashMap<String, Any>()
        val title = formatTitleIllegal(story.title)
        childAdd["/user-$path/$userId/${story.date}/$title"] = storyValues
        database.updateChildren(childAdd)
    }



    override fun delete(userId: String, path: String, title: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        val new = formatTitleIllegal(title)
        childDelete["/user-$path/$userId/$new"] = null

        database.updateChildren(childDelete)
    }

    override fun update(userId: String, storyId: String, story: StoryModel) {

        val storyValues = story.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["stories/$storyId"] = storyValues
        childUpdate["user-stories/$userId/$storyId"] = storyValues

        database.updateChildren(childUpdate)
    }




    fun updateImageRef(userId: String,imageUri: String) {

        val userStories = database.child("user-stories").child(userId)
        val allStories = database.child("stories")

        userStories.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        //Update Users imageUri
                        it.ref.child("profilepic").setValue(imageUri)
                        //Update all donations that match 'it'
                        val story = it.getValue(StoryModel::class.java)
                        allStories.child(story!!.title)
                            .child("profilepic").setValue(imageUri)
                    }
                }
            })
    }

}