package com.turbotech.translatordemo

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@Preview(showBackground = true)
@Composable
fun TranslatorHomePage() {

//   Is it possible to show the language downloading notification on the UI and sentence correction?????????????????

    val context = LocalContext.current
    val xCoroutineScope = rememberCoroutineScope()
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
    val userValueTranslated = remember {
        mutableStateOf("Translated Text...!")
    }
    val userInputValue = remember {
        mutableStateOf("")
    }
    val speakStatus = remember {
        mutableStateOf(false)
    }
    val translators = translatorFn(toTranslateLanguage)

    Surface(
        modifier = Modifier
            .padding(2.dp)
            .clickable {
                dropDwnStatus.value = false
            },
        shape = RoundedCornerShape(13.dp)
    ) {
        ScaffoldFn(
            context,
            speakStatus,
            userValueTranslated,
            xCoroutineScope,
            snackBarHostState,
            fromLanguage,
            dropDwnStatus,
            toLanguage,
            translateLanguageList,
            toTranslateIn,
            userInputValue,
            translators,
            toTranslateLanguage
        )
    }
}

@Composable
private fun ScaffoldFn(
    context: Context,
    speakStatus: MutableState<Boolean>,
    userValueTranslated: MutableState<String>,
    xCoroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    fromLanguage: MutableState<String>,
    dropDwnStatus: MutableState<Boolean>,
    toLanguage: MutableState<String>,
    translateLanguageList: ArrayList<String>,
    toTranslateIn: MutableIntState,
    userInputValue: MutableState<String>,
    translators: Translator,
    toTranslateLanguage: MutableState<String>
) {
    Scaffold(
        topBar = {
            TopAppbarFn(context, speakStatus, userValueTranslated)
        },
        bottomBar = {
            BottomBarFn(
                xCoroutineScope,
                snackBarHostState,
                fromLanguage,
                dropDwnStatus,
                toLanguage,
                translateLanguageList,
                toTranslateIn
            )
        },
        snackbarHost = {
            SnackBarFn(snackBarHostState)
        }
    ) {
        Column {
            TranslationFn(userInputValue, translators, userValueTranslated, context)
            UserInputFn(userInputValue, it, dropDwnStatus)
            TranslatedFn(userValueTranslated)
        }
        TranslationLanguageSelectFN(toLanguage, toTranslateLanguage)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopAppbarFn(
    context: Context,
    speakStatus: MutableState<Boolean>,
    userValueTranslated: MutableState<String>
) {
    lateinit var textToSpeech: TextToSpeech
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
                Toast.makeText(context, "Speaking out...!", Toast.LENGTH_SHORT).show()
                speakStatus.value = true
                // speak out
                textToSpeech = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val speakResult = textToSpeech.setLanguage(Locale.getDefault())
                        if (speakResult == TextToSpeech.LANG_MISSING_DATA || speakResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(
                                context,
                                "Language not supported",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "I'm not knowing what's wrong..!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (speakStatus.value) {
                        textToSpeech.speak(
                            userValueTranslated.value,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            null
                        )
                        speakStatus.value = false
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_mic_external_on_24),
                    contentDescription = "",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
private fun translatorFn(toTranslateLanguage: MutableState<String>): Translator {
    // Creating an translator
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(Locale.ENGLISH.toString())
        .setTargetLanguage(toTranslateLanguage.value)
        .build()
    val translators = Translation.getClient(options)
    return translators
}

@Composable
private fun TranslationLanguageSelectFN(
    toLanguage: MutableState<String>,
    toTranslateLanguage: MutableState<String>
) {
    when (toLanguage.value) {

        "HINDI" -> toTranslateLanguage.value = TranslateLanguage.HINDI
        "TELUGU" -> toTranslateLanguage.value = TranslateLanguage.TELUGU
        "KANNADA" -> toTranslateLanguage.value = TranslateLanguage.KANNADA
        "GUJARATI" -> toTranslateLanguage.value = TranslateLanguage.GUJARATI
        "MARATHI" -> toTranslateLanguage.value = TranslateLanguage.MARATHI
        "TAMIL" -> toTranslateLanguage.value = TranslateLanguage.TAMIL

    }
}

@Composable
private fun TranslatedFn(userValueTranslated: MutableState<String>) {
    Text(
        text = userValueTranslated.value,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, top = 10.dp),
        fontSize = 20.sp,
        color = Color.Black,
        textAlign = TextAlign.Start
    )
}

@Composable
private fun UserInputFn(
    userInputValue: MutableState<String>,
    it: PaddingValues,
    dropDwnStatus: MutableState<Boolean>
) {
    TextField(
        value = userInputValue.value,
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
            }
    )
}

@Composable
private fun TranslationFn(
    userInputValue: MutableState<String>,
    translators: Translator,
    userValueTranslated: MutableState<String>,
    context: Context
) {
    if (userInputValue.value.isNotEmpty()) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translators.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                if (userInputValue.value.isNotEmpty()) {
                    translators.translate(userInputValue.value)
                        .addOnSuccessListener { translatedText ->
                            // Translation successful.
                            userValueTranslated.value = translatedText
                            Log.d("onTranslateError1", userValueTranslated.value)
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
                } else {
                    Toast.makeText(
                        context,
                        "Please enter some text",
                        Toast.LENGTH_SHORT
                    ).show()
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
    } else {
        userValueTranslated.value = ""
    }
}

@Composable
private fun SnackBarFn(snackBarHostState: SnackbarHostState) {
    SnackbarHost(hostState = snackBarHostState) {
        Snackbar(
            snackbarData = it,
            contentColor = Color.White,
            containerColor = Color.DarkGray,
            dismissActionContentColor = Color.Red
        )
    }
}

@Composable
private fun BottomBarFn(
    xCoroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    fromLanguage: MutableState<String>,
    dropDwnStatus: MutableState<Boolean>,
    toLanguage: MutableState<String>,
    translateLanguageList: ArrayList<String>,
    toTranslateIn: MutableIntState
) {
    BottomAppBar(
        modifier = Modifier.height(65.dp),
        containerColor = Color.DarkGray,
    ) {
        BottomBtnRowFn(xCoroutineScope, snackBarHostState, fromLanguage, dropDwnStatus, toLanguage)
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.width(60.dp)) {
        Row {
            DropDownMenuFn(dropDwnStatus, translateLanguageList, toTranslateIn, toLanguage)
        }
    }
}

@Composable
private fun BottomBtnRowFn(
    xCoroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    fromLanguage: MutableState<String>,
    dropDwnStatus: MutableState<Boolean>,
    toLanguage: MutableState<String>
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        FromBtn(xCoroutineScope, snackBarHostState, fromLanguage)

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            painter = painterResource(id = R.drawable.baseline_swap_horiz_24),
            contentDescription = "",
            tint = Color.White,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Spacer(modifier = Modifier.width(4.dp))

        ToBtn(dropDwnStatus, toLanguage)
    }
}

@Composable
private fun FromBtn(
    xCoroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    fromLanguage: MutableState<String>
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
}

@Composable
private fun ToBtn(
    dropDwnStatus: MutableState<Boolean>,
    toLanguage: MutableState<String>
) {
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

@Composable
private fun DropDownMenuFn(
    dropDwnStatus: MutableState<Boolean>,
    translateLanguageList: ArrayList<String>,
    toTranslateIn: MutableIntState,
    toLanguage: MutableState<String>
) {
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
            DropdownMenuItemFn(translateLanguageList, index, toTranslateIn, toLanguage, text, dropDwnStatus)
        }
    }
}

@Composable
private fun DropdownMenuItemFn(
    translateLanguageList: ArrayList<String>,
    index: Int,
    toTranslateIn: MutableIntState,
    toLanguage: MutableState<String>,
    text: String,
    dropDwnStatus: MutableState<Boolean>
) {
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