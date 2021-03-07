package com.chareas.earthquake.Models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Building(
    var id: String? = "",
    var location: MutableMap<String, String> = mutableMapOf(),
    var firstName: String? = "",
    var lastName: String? = "",
    var tel: String? = "",
    var floors: Int? = 1,
    var apartments: Int? = 1,
    var type: String? = "",
    var status: String? = "",
    var details: String? = "",
    var reviewedStatus: String? = "",
    var numberOfReviews: Int? = 0,
    var images: List<String>? = mutableListOf(),
    var registered: Long = System.currentTimeMillis()
) : Parcelable