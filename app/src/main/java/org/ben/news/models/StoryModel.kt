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
                      var linkPreview: String = "",
                      var website: String = "",
                      var outlet: String = "",
                      var author: String = "",
                      var category: String = "",
                      var preview: String = "",
                      var date: String = "", )
                         : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "id" to id,
                                "headline" to headline,
                                "link" to link,
                                "linkPreview" to linkPreview,
                                "website" to website,
                                "outlet" to outlet,
                                "author" to author,
                                "category" to category,
                                "preview" to preview,
                                "date" to date,
                            )
                        }

    }
