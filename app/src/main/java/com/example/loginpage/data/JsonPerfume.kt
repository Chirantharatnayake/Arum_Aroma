package com.example.loginpage.data

// Simple data class representing one perfume entry from perfumes.json
// Matches the structure inside /assets/perfumes.json
// image holds the drawable resource name (e.g. "mperfume1")
// gender is optional filtering field ("men" or "women")

data class JsonPerfume(
    val id: Int,
    val name: String,
    val image: String,
    val price: Double,
    val description: String,
    val gender: String? = null
)

