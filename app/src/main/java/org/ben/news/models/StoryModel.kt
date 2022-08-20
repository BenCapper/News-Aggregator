package org.ben.news.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize


@Parcelize
data class StoryModel(var title: String = "",
                      var date: String = "",
                      var outlet: String = "",
                      var storage_link: String = "",
                      var order: Int = 0,
                      var link: String = "") : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "title" to title,
                                "date" to date,
                                "outlet" to outlet,
                                "storage_link" to storage_link,
                                "order" to order,
                                "link" to link
                            )
                        }

    }
