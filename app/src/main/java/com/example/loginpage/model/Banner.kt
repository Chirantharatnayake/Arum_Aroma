package com.example.loginpage.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Banner(
    @StringRes val titleResId: Int,
    @StringRes val subtitleResId: Int,
    @DrawableRes val imageResId: Int,
    val perfumeId: Int
)
