package com.securitysoftware.netspeak.data.db

object DbContract {

    const val DATABASE_NAME = "netspeak.db"
    const val DATABASE_VERSION = 1

    object BranchTable {
        const val TABLE = "branches"
        const val ID = "id"
        const val NAME = "name"
    }

    object DeviceTypeTable {
        const val TABLE = "device_types"
        const val ID = "id"
        const val NAME = "name"
    }

    object DeviceTable {
        const val TABLE = "devices"
        const val ID = "id"
        const val BRANCH_ID = "branch_id"
        const val TYPE_ID = "type_id"
        const val NAME = "name"
        const val IP = "ip"
    }
}
