package com.turbotech.translatordemo

import android.content.Context
import android.speech.SpeechRecognizer
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.turbotech.translatordemo.viewModel.TranslationVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TranslatorHomePage() {
    val viewModel = viewModel<TranslationVM>()
    val context = LocalContext.current
    val xCoroutineScope = rememberCoroutineScope()

    val snackBarHostState = remember { SnackbarHostState() }
    val dropDwnStatus = remember {
        mutableStateOf(false)
    }
    val fromLanguage = remember {
        mutableStateOf("ENGLISH")
    }
   
    val toTranslateIn = remember {
        mutableIntStateOf(0)
    }


    val speakStatus = remember {
        mutableStateOf(false)
    }
   
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
                TopBarFn(context, viewModel, speakStatus, xCoroutineScope, snackBarHostState)
            },
            floatingActionButton = {
                FloatActBar(context, viewModel)
            },
            bottomBar = {
                BottomBarFn(
                    xCoroutineScope,
                    snackBarHostState,
                    fromLanguage,
                    dropDwnStatus,
                    viewModel
                )
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
                            TransLangListFn(
                                toTranslateIn,
                                viewModel,
                                dropDwnStatus
                            )
                        }
                    }
                }
            },
            snackbarHost = {
               viewModel.SnackBarFn(snackBarHostState)
            }
        ) {
            Column {
                LaunchedEffect(speakStatus.value) {
                    // speak out
                    if (speakStatus.value) {
                        viewModel.textToSpeakFn()
                        speakStatus.value = false
                    }

                }

                TextField(
                    value = viewModel.userInputValue.value.format(Locale.ENGLISH),
                    onValueChange = { textAT ->
                        viewModel.userInputValue.value = textAT
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
                    text = viewModel.userValueTranslated.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, top = 2.dp),
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun TransLangListFn(
    toTranslateIn: MutableIntState,
    viewModel: TranslationVM,
    dropDwnStatus: MutableState<Boolean>
) {
    viewModel.translateLanguageList.forEachIndexed { index, text ->
        DropdownMenuItem(
            text = {
                Text(
                    text = viewModel.translateLanguageList[index],
                    color = Color.White
                )
            },
            onClick = {
                toTranslateIn.intValue = index
                viewModel.toLanguage.value = text
                dropDwnStatus.value = false
            })
    }
}

@Composable
private fun BottomBarFn(
    xCoroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    fromLanguage: MutableState<String>,
    dropDwnStatus: MutableState<Boolean>,
    viewModel: TranslationVM
) {
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
                Text(text = viewModel.toLanguage.value, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "",
                )
            }
        }
    }
}

@Composable
private fun FloatActBar(
    context: Context,
    viewModel: TranslationVM
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(2.dp, Color.Magenta, shape = CircleShape)
            .clickable(
                enabled = true,
                onClick = {
                    if (SpeechRecognizer.isRecognitionAvailable(context)) {
                        viewModel.speechRec(context)
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
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopBarFn(
    context: Context,
    viewModel: TranslationVM,
    speakStatus: MutableState<Boolean>,
    xCoroutineScope: CoroutineScope,
    snackBarHostState: SnackbarHostState
) {
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
                if (viewModel.userInputValue.value.isNotEmpty()) {
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
}