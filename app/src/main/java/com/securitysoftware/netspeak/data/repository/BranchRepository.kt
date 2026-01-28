package com.securitysoftware.netspeak.data.repository

import android.content.Context
import com.securitysoftware.netspeak.data.db.DbContract
import com.securitysoftware.netspeak.data.db.NetSpeakDatabase
import com.securitysoftware.netspeak.data.model.Device

class BranchRepository(context: Context) {

    private val dbHelper = NetSpeakDatabase(context)

    fun findDevicesByBranchName(spokenText: String): List<Device> {

        val devices = mutableListOf<Device>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT d.name, d.ip, t.name
            FROM ${DbContract.DeviceTable.TABLE} d
            JOIN ${DbContract.DeviceTypeTable.TABLE} t
              ON d.${DbContract.DeviceTable.TYPE_ID} = t.${DbContract.DeviceTypeTable.ID}
            JOIN ${DbContract.BranchTable.TABLE} b
              ON d.${DbContract.DeviceTable.BRANCH_ID} = b.${DbContract.BranchTable.ID}
            WHERE LOWER(b.${DbContract.BranchTable.NAME})
            LIKE LOWER(?)
            """,
            arrayOf("%$spokenText%")
        )

        while (cursor.moveToNext()) {
            devices.add(
                Device(
                    id = 0,
                    branchId = 0,
                    type = cursor.getString(2),
                    name = cursor.getString(0),
                    ip = cursor.getString(1)
                )
            )
        }

        cursor.close()
        return devices
    }
}
