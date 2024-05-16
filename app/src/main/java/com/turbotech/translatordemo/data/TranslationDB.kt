package com.turbotech.translatordemo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.turbotech.translatordemo.model.TranslationText

@Database(entities = [TranslationText::class], version = 1, exportSchema = false)
abstract class TranslationDB : RoomDatabase() {
//    Create a function that will give the access to DAO
    abstract fun translationDao(): TranslationDao

}