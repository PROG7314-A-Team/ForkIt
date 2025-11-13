package com.example.forkit.data.local.converters

import androidx.room.TypeConverter
import com.example.forkit.data.local.entities.MealIngredient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromMealIngredientList(value: List<MealIngredient>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toMealIngredientList(value: String): List<MealIngredient> {
        val listType = object : TypeToken<List<MealIngredient>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}

