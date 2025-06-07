package com.example.loginpage.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Perfume(
    val id: Int,
    @StringRes val nameResId: Int,
    @DrawableRes val imageResId: Int,
    val price: Double,
    @StringRes val descriptionResId: Int,
    val originalPrice: Double? = null // Optional: only for discounted items
)
