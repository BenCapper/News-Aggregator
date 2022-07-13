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
        val totalList = ArrayList<StoryModel>()

            for (date in dates) {
                database.child("stories").child(date)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            Timber.i("Firebase Timcast error : ${error.message}")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val children = snapshot.children
                            children.forEach {
                                val story = it.getValue(StoryModel::class.java)
                                totalList.add(story!!)
                            }
                            database.child("stories").child(date)
                                .removeEventListener(this)
                            storyList.value = totalList

                        }
                    })
        }
    }

    override fun search(term: String, dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
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
                                totalList.add(story!!)
                            }
                        }
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun findByOutlet(dates: ArrayList<String>, outlet: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

        for (date in dates) {
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
                                totalList.add(story)
                            }
                        }
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        storyList.value = totalList

                    }
                })
        }
    }

    override fun findOutletNoImage(dates: ArrayList<String>, outlet: String, storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()

        for (date in dates) {

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
                                totalList.add(story)
                            }
                        }
                        database.child("stories").child("Found on: $date")
                            .removeEventListener(this)
                        storyList.value = totalList

                    }
                })
        }
    }


    override fun find(userId: String, path:String,  storyList: MutableLiveData<List<StoryModel>>) {
        val totalList = ArrayList<StoryModel>()
        database.child("user-$path").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(StoryModel::class.java)
                        totalList.add(story!!)
                        Timber.i("user-article=$story")
                    }
                    database.child("user-$path").child(userId)
                        .removeEventListener(this)
                    storyList.value = totalList
                }
            })
    }

    override fun search(term: String, userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>) {

        var totalList = ArrayList<StoryModel>()

        database.child("user-$path").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        if (it.getValue(StoryModel::class.java)?.title!!.contains(term, true) ||
                            it.getValue(StoryModel::class.java)?.outlet!!.contains(term, true) ||
                            it.getValue(StoryModel::class.java)?.date!!.contains(term, true)) {
                            val story = it.getValue(StoryModel::class.java)
                            totalList.add(story!!)
                        }
                    }
                    database.child("user-$path").child(userId)
                        .removeEventListener(this)
                        storyList.value = totalList
                }
            })
    }

    override fun searchByOutlet(dates: ArrayList<String>, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
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
                                totalList.add(story!!)
                            }
                        }
                        database.child("stories").child(date)
                            .removeEventListener(this)
                        storyList.value = totalList
                    }
                })
        }
    }

    override fun searchOutletNoImage(dates: ArrayList<String>, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>) {

        val totalList = ArrayList<StoryModel>()
        for (date in dates) {
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
                                totalList.add(story!!)
                            }
                        }
                        database.child("stories").child("Found on: $date")
                            .removeEventListener(this)
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
        childAdd["/user-$path/$userId/${story.title}"] = storyValues
        database.updateChildren(childAdd)
    }



    override fun delete(userId: String, path: String, title: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()

        childDelete["/user-$path/$userId/$title"] = null

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