package org.ben.news.models

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface StoryStore {
    fun findAll(dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>)
    fun find(userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>)
    fun findById(userId:String, storyId: String, story: MutableLiveData<StoryModel>)
    fun create(userId: String, path:String, story: StoryModel)
    fun delete(userId:String, storyId: String)
    fun update(userId:String, storyId: String, story: StoryModel)
    fun search(term: String, dates: ArrayList<String>, storyList: MutableLiveData<List<StoryModel>>)
    fun search(term: String, userId: String,path: String, storyList: MutableLiveData<List<StoryModel>>)
}