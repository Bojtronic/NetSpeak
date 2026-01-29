package com.securitysoftware.netspeak.admin


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.securitysoftware.netspeak.data.model.Device

@Composable
fun DeviceItem(
    device: Device,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    Color.Transparent
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = device.name,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "IP: ${device.ip}",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tipo: ${device.type}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
