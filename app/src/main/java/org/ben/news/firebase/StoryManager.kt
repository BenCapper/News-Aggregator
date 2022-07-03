package org.ben.news.firebase


import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import org.ben.news.models.StoryModel
import org.ben.news.models.StoryStore
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object StoryManager : StoryStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference


    override fun findAll(dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>) {
        var totalList = ArrayList<StoryModel>()
        Timber.i("DATES1=$dates")
        dates.sort()
        Timber.i("DATES2=$dates")
        dates.reverse()
        Timber.i("DATES3=$dates")

        for (date in dates ) {
            database.child("stories").child("Timcast").child(date)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Timber.i("Firebase building error : ${error.message}")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val localList = ArrayList<StoryModel>()
                        val children = snapshot.children
                        children.forEach {
                            val story = it.getValue(StoryModel::class.java)
                            totalList.add(story!!)
                            localList.add(story!!)
                        }
                        database.child("stories").child("Timcast").child(date)
                            .removeEventListener(this)
                        Timber.i("TOTALLIST=$totalList")
                        storyList.value = totalList
                    }

                })
        }

    }

    override fun findToday(dateYest: String, date: String,storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        database.child("stories").child("Timcast").child(dateYest)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StoryModel>()
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(StoryModel::class.java)
                        totalList.add(story!!)
                        localList.add(story!!)
                        Timber.i("TOTALLIST=$totalList")
                        Timber.i("LOCALLIST=$localList")
                    }
                    database.child("stories").child("Timcast").child(dateYest)
                        .removeEventListener(this)
                }

            })
        database.child("stories").child("Timcast").child(date)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StoryModel>()
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(StoryModel::class.java)
                        totalList.add(story!!)
                        localList.add(story!!)
                    }
                    database.child("stories").child("Timcast").child(date)
                        .removeEventListener(this)
                    storyList.value = totalList

                }
            })

    }



    override fun search(userId: String,term: String, storyList: MutableLiveData<List<StoryModel>>) {

        database.child("user-stories").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase story error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StoryModel>()
                    val children = snapshot.children
                    children.forEach {
                        if (it.getValue(StoryModel::class.java)?.title!!.contains(term) ) {
                            val story = it.getValue(StoryModel::class.java)
                            localList.add(story!!)
                        }
                    }
                    database.child("user-stories").child(userId)
                        .removeEventListener(this)

                    storyList.value = localList
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

    override fun create(story: StoryModel) {
        val key = database.child("story").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        story.title = key
        val storyValues = story.toMap()
        var newDate = story.date.replace(".","-")
        val childAdd = HashMap<String, Any>()
        childAdd["/stories/$newDate/$key"] = storyValues

        database.updateChildren(childAdd)
    }

    override fun delete(userId: String, storyId: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()
        childDelete["/stories/$storyId"] = null
        childDelete["/user-stories/$userId/$storyId"] = null

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