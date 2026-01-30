package com.securitysoftware.netspeak.data.model

data class Device(
    val id: Int,
    val branchId: Int,
    val type: String,
    val name: String,
    val ip: String
)
