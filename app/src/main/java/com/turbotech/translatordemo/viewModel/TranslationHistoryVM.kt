package com.turbotech.translatordemo.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turbotech.translatordemo.model.TranslationText
import com.turbotech.translatordemo.repository.TranslationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslationHistoryVM @Inject constructor(private val translationHistoryRepo: TranslationHistoryRepository) :
    ViewModel() {

    private val _translationHistory = MutableStateFlow<List<TranslationText>>(emptyList())

    val translationHistory = _translationHistory.asStateFlow()

    init{
        viewModelScope.launch {
            translationHistoryRepo.getTranslationHistory().distinctUntilChanged().collect{ historyList ->
                if(historyList.isNotEmpty()){
                    _translationHistory.value = historyList
                }else{
                    Log.d("TranslationHistoryVM", "No translation history found")
                }
            }
        }
    }
    fun insertTranslationText(translationText: TranslationText) = viewModelScope.launch {
        translationHistoryRepo.insertTranslationText(translationText)
    }

//    fun updateTranslationText(translationText: TranslationText) = viewModelScope.launch {
//        translationHistoryRepo.updateTranslationText(translationText)
//    }
//
//    fun deleteTranslationText(translationText: TranslationText) = viewModelScope.launch {
//        translationHistoryRepo.deleteTranslationText(translationText)
//    }

}