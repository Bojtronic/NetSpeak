package com.securitysoftware.netspeak.auth


import com.securitysoftware.netspeak.data.model.UserRole
import com.securitysoftware.netspeak.data.model.UserSession

object AuthManager {

    private var session: UserSession? = null

    // Credenciales hardcodeadas
    private val users = mapOf(
        "ss" to Pair("securitysoftware", UserRole.ADMIN)
    )

    fun login(username: String, password: String): Boolean {
        val user = users[username] ?: return false

        if (user.first == password) {
            session = UserSession(username, user.second)
            return true
        }
        return false
    }

    fun isAdmin(): Boolean {
        return session?.role == UserRole.ADMIN
    }

    fun logout() {
        session = null
    }
}
