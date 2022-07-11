package org.ben.news.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize


@Parcelize
data class UserModel(var id: String = "",
                     var email: String = "",
                      var liked: ArrayList<String> = ArrayList(),
                      var history: ArrayList<String> = ArrayList(),
                      var image: String = "") : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "liked" to liked,
            "history" to history,
            "image" to image,
        )
    }

}