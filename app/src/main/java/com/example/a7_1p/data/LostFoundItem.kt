package com.example.a7_1p.data

data class LostFoundItem(
    val id: Long = 0,
    val type: String,
    val name: String,
    val phone: String,
    val description: String,
    val createdAtMillis: Long,
    val location: String,
    val category: String,
    val imageUri: String
)
