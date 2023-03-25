package org.ben.news.firebase


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import org.ben.news.models.DoubleStoryModel
import org.ben.news.models.OutletModel
import org.ben.news.models.StoryModel
import org.ben.news.models.StoryStore
import timber.log.Timber
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


object StoryManager : StoryStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference


    fun getDate(n:Int): String{
        val now = LocalDate.now()
        val chosen = now.minusDays(n.toLong()).toString().split(('-'))
        val date = chosen[1] + "-" + chosen[2] + "-" + chosen[0].substring(2)
        Timber.i("CHOSEN = $date")
        return date
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

    override fun findAll(date: String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
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
                            story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                            todayList.add(story!!)
                        }
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun findAllDouble(date: String, storyList: MutableLiveData<List<DoubleStoryModel>>) {

        val totalList = ArrayList<DoubleStoryModel>()
        var todayList = mutableListOf<DoubleStoryModel>()
        database.child("doubles").child(date)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(DoubleStoryModel::class.java)
                        todayList.add(story!!)
                    }
                    todayList = todayList.sortedBy{it.order}.toMutableList()
                    todayList.reverse()
                    database.child("doubles").child(date)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                }
            })
    }

    override fun findAllShuffle(date: String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
            val todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                            todayList.add(story!!)
                        }
                        todayList.shuffle()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun findRightShuffle(outlets: List<String>, date: String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        val todayList = mutableListOf<StoryModel>()
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
                    todayList.shuffle()
                    database.child("stories").child(date)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                }
            })
    }

    override fun search(term: String, date: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
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
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun searchDouble(term: String, date: String, storyList: MutableLiveData<List<DoubleStoryModel>>) {
        val totalList = ArrayList<DoubleStoryModel>()
        var todayList = mutableListOf<DoubleStoryModel>()
        database.child("doubles").child(date)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        if (it.getValue(DoubleStoryModel::class.java)?.title1!!.contains(term, true) ||
                            it.getValue(DoubleStoryModel::class.java)?.title2!!.contains(term, true) ||
                            it.getValue(DoubleStoryModel::class.java)?.outlet1!!.contains(term, true) ||
                            it.getValue(DoubleStoryModel::class.java)?.outlet2!!.contains(term, true) ||
                            it.getValue(DoubleStoryModel::class.java)?.titlehead!!.contains(term, true)
                        ) {
                            val story = it.getValue(DoubleStoryModel::class.java)
                            todayList.add(story!!)
                        }
                    }
                    todayList = todayList.sortedBy{it.order}.toMutableList()
                    todayList.reverse()
                    database.child("doubles").child(date)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                }
            })
    }

    override fun findByOutlet(date: String, outlet: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

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
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                        Timber.i("FEEDLIST MANAGER : $totalList")
                    }
                })
    }

    override fun findByOutletFeed(date: String, outlets: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

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
                        for (o in outlets){
                            if(story!!.outlet.contains(o)) {
                                story.title = formatTitle(story.title)
                                todayList.add(story)
                            }
                        }

                    }
                    todayList = todayList.sortedBy{it.order}.toMutableList()
                    todayList.reverse()
                    database.child("stories").child(date)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                    Timber.i("NAMES FOUND MANAGER : $totalList")
                }
            })
    }

    override fun findByOutlets(date: String, outlets: List<String>, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()


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
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList

                    }
                })
    }

    override fun findOutletNoImage(date: String, outlet: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

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
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child("Found on: $date")
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun find(userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
            var todayList = mutableListOf<StoryModel>()
            database.child("user-$path").child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                            todayList.add(story!!)
                            Timber.i("user-article=$story")
                        }
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("user-$path").child(userId)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun findOutlets(userId: String, outletList: MutableLiveData<List<OutletModel>>, callback: () -> Unit) {
        val totalList = ArrayList<OutletModel>()
        var foundList = mutableListOf<OutletModel>()
        database.child("android-outlets").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val outlet = it.getValue(OutletModel::class.java)
                        foundList.add(outlet!!)
                    }
                    database.child("android-outlets").child(userId)
                        .removeEventListener(this)
                    totalList.addAll(foundList)
                    outletList.value = totalList
                    Timber.i("FEEDLIST =$totalList")
                    callback()
                }
            })
    }

    override fun findOutletLinks(userId: String, outletList: MutableLiveData<List<String>>, callback: () -> Unit) {
        val totalList = ArrayList<String>()
        var foundList = mutableListOf<String>()
        database.child("android-outlets").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val outlet = it.getValue(OutletModel::class.java)
                        if (outlet!!.selected){
                            foundList.add(outlet!!.link)

                        }
                    }
                    database.child("android-outlets").child(userId)
                        .removeEventListener(this)
                    totalList.addAll(foundList)
                    outletList.value = totalList
                    Timber.i("NAMES FOUND == ${totalList}")
                    callback()
                }
            })
    }

    override fun find(date: String, userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        var todayList = mutableListOf<StoryModel>()
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
                        todayList.add(story!!)
                        Timber.i("user-article=$story")
                    }
                    todayList = todayList.sortedBy{it.order}.toMutableList()
                    todayList.reverse()
                    database.child("user-$path").child(userId).child(date)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                }
            })
    }

    override fun findLiked( userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        var todayList = mutableListOf<StoryModel>()
        database.child("user-$path").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(StoryModel::class.java)
                        story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                        todayList.add(story!!)
                        Timber.i("user-article=$story")
                    }
                    todayList = todayList.sortedBy{it.date.split('-')[0]}.sortedBy{it.date.split('-')[1]}.sortedBy{it.date.split('-')[2]}.toMutableList()
                    todayList.reverse()
                    database.child("user-$path").child(userId)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                }
            })
    }



    override fun search(date: String,term: String, userId: String, path:String,  storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
            var todayList = mutableListOf<StoryModel>()
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
                                todayList.add(story!!)

                            }
                        }
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("user-$path").child(userId).child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun searchLiked(term: String, userId: String, path:String,  storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        var todayList = mutableListOf<StoryModel>()
        database.child("user-$path").child(userId)
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
                            todayList.add(story!!)

                        }
                    }
                    todayList = todayList.sortedBy{it.order}.toMutableList()
                    todayList.reverse()
                    database.child("user-$path").child(userId)
                        .removeEventListener(this)
                    totalList.addAll(todayList)
                    storyList.value = totalList
                }
            })
    }

    override fun searchByOutlet(date: String, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
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
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun searchByOutlets(date: String, term: String, outlets:List<String>, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
            var todayList = mutableListOf<StoryModel>()
            database.child("stories").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val children = snapshot.children
                        children.forEach {
                            if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) &&
                                it.getValue(StoryModel::class.java)?.outlet!! in outlets ||
                                it.getValue(StoryModel::class.java)?.outlet!!.contains(term, true) &&
                                it.getValue(StoryModel::class.java)?.outlet!! in outlets ||
                                it.getValue(StoryModel::class.java)?.date!!.contains(term, true) &&
                                it.getValue(StoryModel::class.java)?.outlet!! in outlets

                            ) {
                                val story = it.getValue(StoryModel::class.java)
                                story?.title = story?.title?.let { it -> formatTitle(it) }.toString()
                                todayList.add(story!!)
                            }
                        }
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
    }

    override fun searchOutletNoImage(date: String, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
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
                        todayList = todayList.sortedBy{it.order}.toMutableList()
                        todayList.reverse()
                        database.child("stories").child("Found on: $date")
                            .removeEventListener(this)
                        totalList.addAll(todayList)
                        storyList.value = totalList
                    }
                })
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
        val date = story.date
        childAdd["/user-$path/$userId/$date/$title"] = storyValues
        database.updateChildren(childAdd)
    }

    override fun createLiked(userId: String, path:String, story: StoryModel) {
        val storyValues = story.toMap()
        val childAdd = HashMap<String, Any>()
        val title = formatTitleIllegal(story.title)
        childAdd["/user-$path/$userId/$title"] = storyValues
        database.updateChildren(childAdd)
    }

    override fun saveOutlets(userId: String, outlets: List<OutletModel>) {
        val updates = HashMap<String, Any>()

        for (outlet in outlets) {
            val outValues = outlet.toMap()
            updates["/android-outlets/$userId/${outlet.name}"] = outValues
        }

        database.updateChildren(updates)
    }

    override fun delete(day:String, userId: String, path: String, title: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        val new = formatTitleIllegal(title)
        childDelete["/user-$path/$userId/$day/$new"] = null

        database.updateChildren(childDelete)
    }

    override fun deleteLiked(userId: String, path: String, title: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        val new = formatTitleIllegal(title)
        childDelete["/user-$path/$userId/$new"] = null

        database.updateChildren(childDelete)
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