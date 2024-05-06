package com.turbotech.translatordemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.turbotech.translatordemo.ui.theme.TranslatorDemoTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TranslatorDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TranslatorDemo()
                }
            }
        }
    }
}

@Composable
fun TranslatorDemo() {
    val context = LocalContext.current
    val textValue = remember {
        mutableStateOf("Hey, how are you")
    }
    // Create an translator:
    val options = TranslatorOptions.Builder()
//    setSourceLanguage => Locale.ENGLISH.toString() OR TranslateLanguage.ENGLISH
        .setSourceLanguage(Locale.ENGLISH.toString())
        .setTargetLanguage(TranslateLanguage.ARABIC)
        .build()
    val translators = Translation.getClient(options)


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            translators.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    // Model downloaded successfully. Okay to start translating.
                    // (Set a flag, unhide the translation UI, etc.)
                    translators.translate(textValue.value)
                        .addOnSuccessListener { translatedText ->
                            // Translation successful.
                            textValue.value = translatedText
                            Log.d("onTranslateError1", textValue.value)
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
                .addOnFailureListener { exception ->
                    exception.localizedMessage?.let { it1 ->
                        Log.d(
                            "onTranslateErrors2",
                            it1
                        )
                    }
                }
            Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
        }) {
            Icon(Icons.TwoTone.Person, contentDescription = "")
        }
        Spacer(modifier = Modifier.height(25.dp))
        Text(text = textValue.value, fontSize = 24.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TranslatorDemoTheme {
        TranslatorDemo()
    }
}