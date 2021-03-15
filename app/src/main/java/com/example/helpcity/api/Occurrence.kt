package com.example.helpcity.api

data class Occurrence(
    val id: String,
    val type: String,
    val description: String,
    val lat: String,
    val lng: String,
    val image: String,
    val user_id: String
)
