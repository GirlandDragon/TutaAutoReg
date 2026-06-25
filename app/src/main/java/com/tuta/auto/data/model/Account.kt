package com.tuta.auto.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["email"], unique = true)]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val accessToken: String = "",
    val recoveryCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "active"
)
