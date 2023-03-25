package org.ben.news.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.android.parcel.Parcelize


@Parcelize
data class OutletModel(var name: String = "",
                       var selected: Boolean = false,
                       var region: String = "") : Parcelable
    {
                        @Exclude
                        fun toMap(): Map<String, Any?> {
                            return mapOf(
                                "name" to name,
                                "selected" to selected,
                                "region" to region
                            )
                        }
    }
