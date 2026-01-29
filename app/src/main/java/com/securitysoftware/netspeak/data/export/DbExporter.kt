package com.securitysoftware.netspeak.data.export

import android.content.Context
import com.securitysoftware.netspeak.data.db.DbContract
import com.securitysoftware.netspeak.data.db.NetSpeakDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import android.net.Uri


class DbExporter(private val context: Context) {

    private val dbHelper = NetSpeakDatabase(context)

    fun exportToUri(uri: Uri) {

        val db = dbHelper.readableDatabase
        val root = JSONObject()
        val branchesArray = JSONArray()

        val branchCursor = db.query(
            DbContract.BranchTable.TABLE,
            null, null, null, null, null, null
        )

        while (branchCursor.moveToNext()) {

            val branchId = branchCursor.getInt(
                branchCursor.getColumnIndexOrThrow(DbContract.BranchTable.ID)
            )

            val branchObj = JSONObject().apply {
                put(
                    "name",
                    branchCursor.getString(
                        branchCursor.getColumnIndexOrThrow(DbContract.BranchTable.NAME)
                    )
                )
            }

            val devicesArray = JSONArray()

            val deviceCursor = db.rawQuery(
                """
            SELECT d.name, d.ip, t.name
            FROM ${DbContract.DeviceTable.TABLE} d
            JOIN ${DbContract.DeviceTypeTable.TABLE} t
              ON d.${DbContract.DeviceTable.TYPE_ID} = t.${DbContract.DeviceTypeTable.ID}
            WHERE d.${DbContract.DeviceTable.BRANCH_ID} = ?
            """,
                arrayOf(branchId.toString())
            )

            while (deviceCursor.moveToNext()) {
                devicesArray.put(
                    JSONObject().apply {
                        put("name", deviceCursor.getString(0))
                        put("ip", deviceCursor.getString(1))
                        put("type", deviceCursor.getString(2))
                    }
                )
            }

            deviceCursor.close()
            branchObj.put("devices", devicesArray)
            branchesArray.put(branchObj)
        }

        branchCursor.close()
        root.put("branches", branchesArray)

        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(root.toString(2).toByteArray())
        }
    }

    fun exportToJson(): File {

        val db = dbHelper.readableDatabase
        val root = JSONObject()
        val branchesArray = JSONArray()

        val branchCursor = db.query(
            DbContract.BranchTable.TABLE,
            null, null, null, null, null, null
        )

        while (branchCursor.moveToNext()) {

            val branchId = branchCursor.getInt(
                branchCursor.getColumnIndexOrThrow(DbContract.BranchTable.ID)
            )

            val branchObj = JSONObject().apply {
                put(
                    "name",
                    branchCursor.getString(
                        branchCursor.getColumnIndexOrThrow(DbContract.BranchTable.NAME)
                    )
                )
            }

            val devicesArray = JSONArray()

            val deviceCursor = db.rawQuery(
                """
                SELECT d.name, d.ip, t.name
                FROM ${DbContract.DeviceTable.TABLE} d
                JOIN ${DbContract.DeviceTypeTable.TABLE} t
                  ON d.${DbContract.DeviceTable.TYPE_ID} = t.${DbContract.DeviceTypeTable.ID}
                WHERE d.${DbContract.DeviceTable.BRANCH_ID} = ?
                """,
                arrayOf(branchId.toString())
            )

            while (deviceCursor.moveToNext()) {
                devicesArray.put(
                    JSONObject().apply {
                        put("name", deviceCursor.getString(0))
                        put("ip", deviceCursor.getString(1))
                        put("type", deviceCursor.getString(2))
                    }
                )
            }

            deviceCursor.close()
            branchObj.put("devices", devicesArray)
            branchesArray.put(branchObj)
        }

        branchCursor.close()
        root.put("branches", branchesArray)

        val file = File(context.cacheDir, "netspeak_backup.json")
        file.writeText(root.toString(2))

        return file
    }
}
