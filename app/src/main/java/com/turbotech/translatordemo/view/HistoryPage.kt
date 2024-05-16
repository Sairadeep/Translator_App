package com.turbotech.translatordemo.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbotech.translatordemo.viewModel.TranslationHistoryVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationHistoryPage(translationHistoryVM: TranslationHistoryVM) {
    val historyList = translationHistoryVM.translationHistory.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "History", fontSize = 24.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LazyColumn {
                items(historyList) { historyText ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp),
                        shape = RoundedCornerShape(2.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 2.dp,
                            focusedElevation = 2.dp
                        ),
                        border = BorderStroke(2.dp, Color.Magenta)
                    ) {
                        Text(text = historyText.userInputText)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = historyText.translatedText)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(text = historyText.translatedTime.toString())
                    }
                }
            }
        }
    }
}