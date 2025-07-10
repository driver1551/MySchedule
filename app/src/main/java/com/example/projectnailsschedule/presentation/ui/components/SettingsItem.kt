package com.example.projectnailsschedule.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsItem(
    name: String,
    description: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        trailingContent?.invoke()
    }
}

@Composable
@Preview(showBackground = true)
fun SettingsItemPreview(
) {
    SettingsItem(
        name = "Настройка",
        description = "Описание",
        onClick = { }
    )
}