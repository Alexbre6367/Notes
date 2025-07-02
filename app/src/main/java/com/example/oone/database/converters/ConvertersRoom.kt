package com.example.oone.database.converters

import androidx.room.TypeConverter

class ConvertersRoom {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<String> = data.split(",").filter { it.isNotBlank() }
}