package org.ben.news.firebase


import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.ben.news.models.StoryModel
import org.ben.news.models.StoryStore
import org.ben.news.models.UserModel
import org.ben.news.models.UserStore
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object UserManager : UserStore {

    var database: DatabaseReference = FirebaseDatabase.getInstance().reference


    override fun findById(userId: String, storyId: String, story: MutableLiveData<StoryModel>) {

        database.child("users").child(userId)
            .child(storyId).get().addOnSuccessListener {
                story.value = it.getValue(StoryModel::class.java)
                Timber.i("firebase Got value ${it.value}")
            }.addOnFailureListener{
                Timber.e("firebase Error getting data $it")
            }
    }

    

    override fun create(firebaseUser: FirebaseUser){
        Timber.i("Firebase DB Reference : ${StoryManager.database}")
        val user: UserModel = UserModel(firebaseUser.uid)
        val userValues = user.toMap()

        val childAdd = HashMap<String, Any>()
        childAdd["/users/${user.id}"] = userValues


        StoryManager.database.updateChildren(childAdd)
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