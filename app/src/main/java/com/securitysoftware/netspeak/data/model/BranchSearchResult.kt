package com.securitysoftware.netspeak.data.model

data class BranchSearchResult(
    val branch: Branch,
    val devices: List<Device>
)
