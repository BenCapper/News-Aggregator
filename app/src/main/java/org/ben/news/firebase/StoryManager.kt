package org.ben.news.firebase


import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.ben.news.models.StoryModel
import org.ben.news.models.StoryStore
import timber.log.Timber
import java.util.*


object StoryManager : StoryStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference


    override fun findAll(storyList: MutableLiveData<List<StoryModel>>) {
        database.child("stories")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase building error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StoryModel>()
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(StoryModel::class.java)
                        localList.add(story!!)
                    }
                    database.child("stories")
                        .removeEventListener(this)

                    storyList.value = localList
                }
            })
    }

    override fun findAll(userId: String, storyList: MutableLiveData<List<StoryModel>>) {

        database.child("user-stories").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Timber.i("Firebase story error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val localList = ArrayList<StoryModel>()
                    val children = snapshot.children
                    children.forEach {
                        val story = it.getValue(StoryModel::class.java)
                        localList.add(story!!)

                    }
                    database.child("user-stories").child(userId)
                        .removeEventListener(this)

                    storyList.value = localList
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
                        if (it.getValue(StoryModel::class.java)?.headline!!.contains(term) ) {
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

    override fun create(firebaseUser: MutableLiveData<FirebaseUser>, story: StoryModel) {
        Timber.i("Firebase DB Reference : $database")

        val uid = firebaseUser.value!!.uid
        val key = database.child("story").push().key
        if (key == null) {
            Timber.i("Firebase Error : Key Empty")
            return
        }
        story.id = key
        val storyValues = story.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/stories/$key"] = storyValues
        childAdd["/user-stories/$uid/$key"] = storyValues

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
                        allStories.child(story!!.id)
                            .child("profilepic").setValue(imageUri)
                    }
                }
            })
    }

}