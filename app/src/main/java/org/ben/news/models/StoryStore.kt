package org.ben.news.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface StoryStore {
    fun findAll(storyList: MutableLiveData<List<StoryModel>>)
    fun findAll(userId:String, storyList: MutableLiveData<List<StoryModel>>)
    fun findAllByDateOutlet(date: String, outlet: String, storyList: MutableLiveData<List<StoryModel>>)
    fun findById(userId:String, storyId: String, story: MutableLiveData<StoryModel>)
    fun create(story: StoryModel)
    fun delete(userId:String, storyId: String)
    fun update(userId:String, storyId: String, story: StoryModel)
    fun search(userId: String,term: String, storyList: MutableLiveData<List<StoryModel>>)
}