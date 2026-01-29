package com.securitysoftware.netspeak.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.securitysoftware.netspeak.auth.AuthManager

class AdminActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isAdmin()) {
            finish()
            return
        }

        setContent {
            AdminScreen()
        }
    }
}