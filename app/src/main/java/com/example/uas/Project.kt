package com.example.uas

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val tujuan: String,
    val startDate: String,
    val endDate: String,
    val supervisor: String,
    val anggota: String,
    val status: String,
    val notes: String
)
