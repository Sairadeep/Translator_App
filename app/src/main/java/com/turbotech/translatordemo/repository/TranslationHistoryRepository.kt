package com.turbotech.translatordemo.repository

import com.turbotech.translatordemo.data.TranslationDao
import com.turbotech.translatordemo.model.TranslationText
import javax.inject.Inject

class TranslationHistoryRepository @Inject constructor(private val translationDao: TranslationDao) {

    suspend fun insertTranslationText(translationText: TranslationText) =
        translationDao.insert(translationText)

    suspend fun updateTranslationText(translationText: TranslationText) =
        translationDao.update(translationText)

    fun getTranslationHistory() = translationDao.getHistory()

    suspend fun deleteTranslationText(translationText: TranslationText) =
        translationDao.delete(translationText)

}