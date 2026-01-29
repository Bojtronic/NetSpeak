package com.securitysoftware.netspeak.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.securitysoftware.netspeak.auth.AuthManager

@Composable
fun LoginScreen(onSuccess: () -> Unit) {

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Acceso Administrador", fontSize = 20.sp)

        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            label = { Text(text = "Usuario") }
        )

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text(text = "Contraseña") }
        )

        Button(onClick = {
            if (AuthManager.login(user, pass)) {
                onSuccess()
            } else {
                error = true
            }
        }) {
            Text("Ingresar")
        }

        if (error) {
            Text("Credenciales incorrectas", color = Color.Red)
        }
    }
}
