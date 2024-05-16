package com.turbotech.translatordemo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "translation_textTbl")
data class TranslationText(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    @ColumnInfo(name = "user_input_text")
    val userInputText: String,
    @ColumnInfo(name = "translated_text")
    val translatedText: String,
    @ColumnInfo(name = "translated_time")
    val translatedTime: Long = System.currentTimeMillis()
)