package org.ben.news.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface UserStore {
    fun findById(userId:String, storyId: String, story: MutableLiveData<StoryModel>)
    fun create(firebaseUser: FirebaseUser)
    fun delete(userId:String, storyId: String)
    fun update(userId:String, user: UserModel)

}