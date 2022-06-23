package org.ben.news.repository

import com.google.firebase.database.core.Context
import org.ben.news.models.StoryModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.io.IOException

class Repo {

    companion object {
        var instance: Repo? = null
        lateinit var mContext: Context
    }

    fun getInstance(context: Context): Repo {
        mContext = context
        if (instance == null)
            instance = Repo()

        return instance!!
    }

    fun getStories(): MutableList<StoryModel> {
        val listData = mutableListOf<StoryModel>()
        try {
            val url = "https://timcast.com/news/"
            val doc = Jsoup.connect(url).get()
            val stories = doc.select(".article-block")
            //Timber.i("stories = $stories")
            val storiesSize = stories.size
            for (i in 0 until storiesSize) {
                val title = stories.select("h2")
                    .eq(i)
                    .text()
                Timber.i("TITLE = $title")
                var date = stories.select("div.summary")
                    .eq(i)
                    .text()
                val regex ="""[0-9]{2}.[0-9]{2}.[0-9]{2}""".toRegex()
                date = regex.find(date)?.value.toString()
                Timber.i("DATE = $date")
                date = ""
                val author = stories.select("span.auth")
                    .eq(i)
                    .text()
                    .drop(2)
                Timber.i("AUTHOR = $author")
                val link = stories.select("a.image")
                    .select("a")
                    .eq(i)
                    .attr("href")
                Timber.i("LINK = $link")
                val image = stories.select("a")
                    .select("img")
                    .eq(i)
                    .attr("src")
                Timber.i("IMAGE = $image")
                listData.add(StoryModel(i.toString(), title, link, url, author, "", image, date))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return listData
    }
}