package org.ben.news.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize


@Parcelize
data class StoryModel(var title: String = "",
                      var author: String = "",
                      var img_name: String = "",
                      var img_src: String = "",
                      var preview: String = "",
                      var date: String = "",
                      var outlet: String = "",
                      var link: String = "") : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "title" to title,
                                "author" to author,
                                "img_name" to img_name,
                                "img_src" to img_src,
                                "preview" to preview,
                                "date" to date,
                                "outlet" to outlet,
                                "link" to link
                            )
                        }

    }
