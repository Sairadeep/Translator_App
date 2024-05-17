package com.turbotech.translatordemo.viewModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
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
//    val textToSpeech = remember { TextToSpeech(context) {} }
    lateinit var textToSpeech :TextToSpeech
    val toLanguage =
        mutableStateOf("TELUGU")
    val translateLanguageList =
        arrayListOf("HINDI", "TELUGU", "KANNADA", "GUJARATI", "MARATHI", "TAMIL")

    private val _translationHistory = MutableStateFlow<List<TranslationText>>(emptyList())
    val translationHistory = _translationHistory.asStateFlow() // read-only view of a mutable state
    val userInputValue = mutableStateOf("")
    val userValueTranslated = mutableStateOf("Translated Text...!")


    init{
        viewModelScope.launch {
            translationHistoryRepo.getTranslationHistory().distinctUntilChanged().collect{ historyList ->
                if(historyList.isNotEmpty()){
                    _translationHistory.value = historyList
                }else{
                    Log.d("TranslationHistoryVM", "No translation history found")
                }
            }
            translationFn( userInputValue, userValueTranslated)
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


    fun textToSpeakFn() {

        val speakResult =
            textToSpeech.setLanguage(Locale.getDefault())
        if (speakResult == TextToSpeech.LANG_MISSING_DATA || speakResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.d("textToSpeechStatus", "Language not supported..!")
        }
        textToSpeech.speak(
            userInputValue.value,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }

    private fun translatorFn(): Translator {
        // Creating an translator
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(Locale.ENGLISH.toString())
            .setTargetLanguage(toLangFn())
            .build()
        val translators = Translation.getClient(options)
        return translators
    }

    fun translationFn(
        userInputValue: MutableState<String>,
        userValueTranslated: MutableState<String>
    ) {
        val translators = translatorFn()

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
                        insertTranslationText(
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

    private fun toLangFn() : String {
        return when (toLanguage.value) {
            "HINDI" -> TranslateLanguage.HINDI
            "TELUGU" -> TranslateLanguage.TELUGU
            "KANNADA" -> TranslateLanguage.KANNADA
            "GUJARATI" -> TranslateLanguage.GUJARATI
            "MARATHI" -> TranslateLanguage.MARATHI
            "TAMIL" -> TranslateLanguage.TAMIL
            else ->
                TranslateLanguage.ENGLISH
        }
    }

    fun speechRec(
        context: Context
    ) {
        val recognizerIntent1 = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizerIntent1.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizerIntent1.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        speechRecognizer.setRecognitionListener(object :
            RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("OnReadyForSpeech", "User Ready for speaking.")
            }

            override fun onBeginningOfSpeech() {
                Log.d(
                    "onBeginningOfSpeech",
                    "The user has started to speak."
                )
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("OnRmsChanged", "Change in the level of sound")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d(
                    "OnBufferReceived",
                    "More sound has been received."
                )
            }

            override fun onEndOfSpeech() {
                Log.d(
                    "onEndOfSpeech",
                    "Called after the user stops speaking."
                )
                speechRecognizer.stopListening()
            }

            override fun onError(error: Int) {
                Log.d(
                    "OnError",
                    "An network or recognition error occurred."
                )
            }

            override fun onResults(results: Bundle?) {
                val speechResults =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!speechResults.isNullOrEmpty()) {
                    speechRecognizer.stopListening()
                    userInputValue.value =
                        speechResults[0].format(Locale.getDefault())
                    Log.d(
                        "Recognized_Text",
                        "Current Value : ${speechResults[0].uppercase()}"
                    )
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d(
                    "OnPartialResults",
                    "Called when partial recognition results are available."
                )
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(
                    "OnEvent",
                    "Reserved for adding future events $eventType"
                )
            }
        })
        speechRecognizer.startListening(recognizerIntent1)
    }


}

