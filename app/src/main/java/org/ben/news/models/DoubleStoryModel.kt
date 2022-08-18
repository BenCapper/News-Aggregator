package org.ben.news.models

import android.os.Parcelable
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

