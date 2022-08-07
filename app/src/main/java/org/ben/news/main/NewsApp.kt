package org.ben.news.main

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.preference.PreferenceManager
import org.ben.news.firebase.StoryManager
import org.ben.news.models.StoryStore
import timber.log.Timber


class NewsApp : Application() {

    private lateinit var stories: StoryStore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val night = sharedPreferences.contains("night_mode")
        if (night){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        val light = sharedPreferences.contains("light_mode")
        if (light){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }
        Timber.plant(Timber.DebugTree())
        stories = StoryManager
        Timber.i("News App Started")
    }
}