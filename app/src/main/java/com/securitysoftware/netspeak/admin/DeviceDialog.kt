package com.securitysoftware.netspeak.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.securitysoftware.netspeak.data.model.Device
import com.securitysoftware.netspeak.data.model.DeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDialog(
    title: String,
    device: Device? = null,
    deviceTypes: List<DeviceType>,
    onConfirm: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(device?.name ?: "") }
    var ip by remember { mutableStateOf(device?.ip ?: "") }

    var expanded by remember { mutableStateOf(false) }

    if (deviceTypes.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No hay tipos de dispositivo") },
            text = { Text("Debes crear al menos un tipo antes de agregar un dispositivo.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Aceptar")
                }
            }
        )
        return
    }

    var selectedType by remember {
        mutableStateOf(
            deviceTypes.firstOrNull { it.name == device?.type }
                ?: deviceTypes.first()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && ip.isNotBlank(),
                onClick = {
                    onConfirm(
                        name,
                        ip,
                        selectedType.id
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del dispositivo") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP") },
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {

                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de dispositivo") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        deviceTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
}
