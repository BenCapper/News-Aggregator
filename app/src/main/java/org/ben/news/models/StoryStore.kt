package org.ben.news.models

import androidx.lifecycle.MutableLiveData

interface StoryStore {
    fun findAll(date: String, storyList: MutableLiveData<List<StoryModel>>)
    fun findAllDouble(date: String, storyList: MutableLiveData<List<DoubleStoryModel>>)
    fun findAllShuffle(date: String, storyList: MutableLiveData<List<StoryModel>>)
    fun findRightShuffle(outlets: List<String>, date: String, storyList: MutableLiveData<List<StoryModel>>)
    fun find(userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>)
    fun find(date: String, userId: String, path:String, storyList: MutableLiveData<List<StoryModel>>)
    fun findByOutlet(date: String, outlet: String, storyList: MutableLiveData<List<StoryModel>>)
    fun findByOutlets(date: String, outlets: List<String>, storyList: MutableLiveData<List<StoryModel>>)
    fun findOutletNoImage(date: String, outlet: String, storyList: MutableLiveData<List<StoryModel>>)
    fun findById(userId:String, storyId: String, story: MutableLiveData<StoryModel>)
    fun create(userId: String, path:String, story: StoryModel)
    fun delete(day:String, userId: String, path: String, title: String)
    fun search(term: String, date: String, storyList: MutableLiveData<List<StoryModel>>)
    fun search(date: String,term: String, userId: String, path:String,  storyList: MutableLiveData<List<StoryModel>>)
    fun searchDouble(term: String, date: String, storyList: MutableLiveData<List<DoubleStoryModel>>)
    fun searchByOutlet(date: String, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>)
    fun searchByOutlets(date: String, term: String, outlets:List<String>, storyList: MutableLiveData<List<StoryModel>>)
    fun searchOutletNoImage(date: String, term: String, outlet:String, storyList: MutableLiveData<List<StoryModel>>)
}