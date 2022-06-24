package org.ben.news.main

import android.app.Application
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryStore
import timber.log.Timber


class NewsApp : Application() {

    private lateinit var stories: StoryStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        stories = StoryManager
        Timber.i("News App Started")
    }
}