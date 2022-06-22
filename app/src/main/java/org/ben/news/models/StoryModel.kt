package org.ben.news.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize
import java.sql.Time
import java.time.Instant
import java.util.*

@Parcelize
data class StoryModel(var id: String = "",
                      var headline: String = "",
                      var link: String = "",
                      var website: String = "",
                      var author: String = "",
                      var category: String = "",
                      var preview: String = "",
                      var time: Time = Time.from(Instant.now()) as Time,
                      var date: Date = Date.from(Instant.now()), )
                         : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "id" to id,
                                "headline" to headline,
                                "link" to link,
                                "website" to website,
                                "author" to author,
                                "category" to category,
                                "preview" to preview,
                                "time" to time,
                                "date" to date,
                            )
                        }

    }
