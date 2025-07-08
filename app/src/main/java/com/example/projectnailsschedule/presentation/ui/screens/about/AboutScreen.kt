package com.example.projectnailsschedule.presentation.ui.screens.about

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.projectnailsschedule.BuildConfig
import com.example.projectnailsschedule.presentation.ui.theme.AppTheme

@Composable
fun AboutScreen(
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Запись клиентов",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Версия ${BuildConfig.VERSION_NAME}")

        Spacer(modifier = Modifier.height(24.dp))

        AsyncImage(
            model = "https://myschedule.myddns.me/ic_launcher.png",
            contentDescription = "Иконка приложения",
            onError = { error -> Log.e("AsyncImage", "$error") },
            modifier = Modifier
                .size(128.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreviewLight() {
    AppTheme {
        AboutScreen(
        )
    }
}

