package com.securitysoftware.netspeak.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.securitysoftware.netspeak.R
import com.securitysoftware.netspeak.admin.AdminActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen(
                onSuccess = {
                    startActivity(
                        Intent(this, AdminActivity::class.java)
                    )
                    finish()
                }
            )
        }
    }
}