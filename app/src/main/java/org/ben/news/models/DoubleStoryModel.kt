package org.ben.news.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DoubleStoryModel(var titlehead: String = "",
                            var title1: String = "",
                            var title2: String = "",
                            var date1: String = "",
                            var date2: String = "",
                            var outlet1: String = "",
                            var outlet2: String = "",
                            var storage_link1: String = "",
                            var storage_link2: String = "",
                            var link1: String = "",
                            var link2: String = "",
                            var order: Int = 0, ) : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "titlehead" to titlehead,
            "title1" to title1,
            "title2" to title2,
            "date1" to date1,
            "date2" to date2,
            "outlet1" to outlet1,
            "outlet2" to outlet2,
            "storage_link1" to storage_link1,
            "storage_link2" to storage_link2,
            "link1" to link1,
            "link2" to link2,
            "order" to order
        )
    }

}
