package com.turbotech.translatordemo

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.google.mlkit.nl.translate.TranslateLanguage
import com.turbotech.translatordemo.viewModel.TranslationVM
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorHomePage(translationVM: TranslationVM) {

    val context = LocalContext.current
    val xCoroutineScope = rememberCoroutineScope()
    lateinit var recognizerIntent: Intent
    val textToSpeech = remember { TextToSpeech(context) {} }
    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val translateLanguageList =
        arrayListOf("HINDI", "TELUGU", "KANNADA", "GUJARATI", "MARATHI", "TAMIL")
    val snackBarHostState = remember { SnackbarHostState() }
    val dropDwnStatus = remember {
        mutableStateOf(false)
    }
    val fromLanguage = remember {
        mutableStateOf("ENGLISH")
    }
    val toLanguage = remember {
        mutableStateOf("TELUGU")
    }
    val toTranslateIn = remember {
        mutableIntStateOf(0)
    }
    val toTranslateLanguage = remember {
        mutableStateOf("")
    }
    val userInputValue = remember {
        mutableStateOf("")
    }
    val userValueTranslated = remember {
        mutableStateOf("Translated Text...!")
    }
    val speakStatus = remember {
        mutableStateOf(false)
    }
    val translators = translationVM.translatorFn(toTranslateLanguage)
    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(
        modifier = Modifier
            .padding(2.dp)
            .clickable {
                dropDwnStatus.value = false
                keyboardController?.hide()
            },
        shape = RoundedCornerShape(13.dp)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Translator App", fontSize = 24.sp)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = {
                            Toast.makeText(
                                context,
                                "History coming soon...!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_history_24),
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        IconButton(onClick = {
                            if (userInputValue.value.isNotEmpty()) {
                                speakStatus.value = true
                                Toast.makeText(
                                    context,
                                    "Speaking out...!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                xCoroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Provide some text to translate and speak",
                                        duration = SnackbarDuration.Short,
                                        withDismissAction = true
                                    )
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_speaker_24),
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Magenta, shape = CircleShape)
                        .clickable(
                            enabled = true,
                            onClick = {
                                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                                    recognizerIntent =
                                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                                    recognizerIntent.putExtra(
                                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                    )
                                    recognizerIntent.putExtra(
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
                                    speechRecognizer.startListening(recognizerIntent)
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "Speech recognizer not available..!",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_mic_external_on_24),
                        contentDescription = ""
                    )
                }
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.height(65.dp),
                    containerColor = Color.DarkGray,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Button(
                            onClick = {
                                xCoroutineScope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = "Currently, we are supporting 'English' to 6 different languages translation  only..!",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.size(width = 180.dp, height = 45.dp)
                        ) {
                            Text(text = fromLanguage.value, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Icon(
                            painter = painterResource(id = R.drawable.baseline_swap_horiz_24),
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = {
                                dropDwnStatus.value = true
                            },
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.size(width = 180.dp, height = 45.dp)
                        ) {
                            Text(text = toLanguage.value, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "",
                            )
                        }
                    }
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(60.dp)) {
                    Row {
                        DropdownMenu(
                            expanded = dropDwnStatus.value,
                            onDismissRequest = { dropDwnStatus.value = false },
                            modifier = Modifier.background(color = Color.DarkGray),
                            offset = DpOffset(x = 229.dp, y = (-10).dp),
                            properties = PopupProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false
                            )
                        )
                        {
                            translateLanguageList.forEachIndexed { index, text ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = translateLanguageList[index],
                                            color = Color.White
                                        )
                                    },
                                    onClick = {
                                        toTranslateIn.intValue = index
                                        toLanguage.value = text
                                        dropDwnStatus.value = false
                                    })
                            }
                        }
                    }
                }
            },
            snackbarHost = {
               translationVM.SnackBarFn(snackBarHostState)
            }
        ) {
            Column {
                LaunchedEffect(speakStatus.value) {
                    // speak out
                    if (speakStatus.value) {
                        translationVM.textToSpeakFn(textToSpeech, userValueTranslated.value)
                        speakStatus.value = false
                    }

                }

                translationVM.TranslationFn(translators, userInputValue, userValueTranslated, translationVM)

                TextField(
                    value = userInputValue.value.format(Locale.ENGLISH),
                    onValueChange = { textAT ->
                        userInputValue.value = textAT
                    },
                    placeholder = {
                        Text(
                            text = "Please type something..!", fontSize = 20.sp,
                            color = Color.Black,
                            fontStyle = FontStyle.Italic
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(it)
                        .clickable {
                            dropDwnStatus.value = false
                        },
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = true
                    )
                )
                Text(
                    text = userValueTranslated.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, top = 2.dp),
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )
            }
            when (toLanguage.value) {
                "HINDI" -> toTranslateLanguage.value = TranslateLanguage.HINDI
                "TELUGU" -> toTranslateLanguage.value = TranslateLanguage.TELUGU
                "KANNADA" -> toTranslateLanguage.value = TranslateLanguage.KANNADA
                "GUJARATI" -> toTranslateLanguage.value = TranslateLanguage.GUJARATI
                "MARATHI" -> toTranslateLanguage.value = TranslateLanguage.MARATHI
                "TAMIL" -> toTranslateLanguage.value = TranslateLanguage.TAMIL
            }
        }
    }
}




