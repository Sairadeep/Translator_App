package com.turbotech.translatordemo.viewModel

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.turbotech.translatordemo.model.TranslationText
import com.turbotech.translatordemo.repository.TranslationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TranslationVM @Inject constructor(private val translationHistoryRepo: TranslationHistoryRepository) :
    ViewModel() {

    private val _translationHistory = MutableStateFlow<List<TranslationText>>(emptyList())

    val translationHistory = _translationHistory.asStateFlow() // read-only view of a mutable state

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

    private fun insertTranslationText(translationText: TranslationText) = viewModelScope.launch {
        translationHistoryRepo.insertTranslationText(translationText)
    }

//    fun updateTranslationText(translationText: TranslationText) = viewModelScope.launch {
//        translationHistoryRepo.updateTranslationText(translationText)
//    }
//
//    fun deleteTranslationText(translationText: TranslationText) = viewModelScope.launch {
//        translationHistoryRepo.deleteTranslationText(translationText)
//    }

    fun textToSpeakFn(textToSpeech: TextToSpeech, userValueTranslated: String) {
        val speakResult =
            textToSpeech.setLanguage(Locale.getDefault())
        if (speakResult == TextToSpeech.LANG_MISSING_DATA || speakResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.d("textToSpeechStatus", "Language not supported..!")
        }
        textToSpeech.speak(
            userValueTranslated,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }

    @Composable
    fun translatorFn(toTranslateLanguage: MutableState<String>): Translator {
        // Creating an translator
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(Locale.ENGLISH.toString())
            .setTargetLanguage(toTranslateLanguage.value)
            .build()
        val translators = Translation.getClient(options)
        return translators
    }

    @Composable
    fun TranslationFn(
        translators: Translator,
        userInputValue: MutableState<String>,
        userValueTranslated: MutableState<String>,
        translationVM: TranslationVM
    ) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translators.downloadModelIfNeeded(conditions).addOnSuccessListener {
            if (userInputValue.value.isNotEmpty()) {
                translators.translate(userInputValue.value)
                    .addOnSuccessListener { translatedText ->
                        // Translation successful.
                        userValueTranslated.value = translatedText
                        Log.d("onTranslateError1", userValueTranslated.value)
                        // insertion ki vere logic radham
                        translationVM.insertTranslationText(
                            TranslationText(
                                userInputText = userInputValue.value,
                                translatedText = translatedText
                            )
                        )
                    }
                    .addOnFailureListener { exception ->
                        // Error.
                        exception.localizedMessage?.let { it1 ->
                            Log.d(
                                "onTranslateErrors",
                                it1
                            )
                        }
                    }
            }
//                    else {
//                        Toast.makeText(
//                            context,
//                            "Please enter some text",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
        }
            .addOnFailureListener { exception ->
                exception.localizedMessage?.let { it1 ->
                    Log.d(
                        "onTranslateErrors2",
                        it1
                    )
                }
            }
    }

    @Composable
     fun SnackBarFn(snackBarHostState: SnackbarHostState) {
        SnackbarHost(hostState = snackBarHostState) {
            Snackbar(
                snackbarData = it,
                contentColor = Color.White,
                containerColor = Color.DarkGray,
                dismissActionContentColor = Color.White
            )
        }
    }

}