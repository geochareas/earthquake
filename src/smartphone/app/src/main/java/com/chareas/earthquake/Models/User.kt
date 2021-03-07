package com.chareas.earthquake.Models


data class User(
    var uid: String? = "",
    var email: String? = "",
    var displayName: String? = "",
    var photoUrl: String? = "",
    var civilEngineer: Boolean = false,
    var registered: Long? = 0L
)