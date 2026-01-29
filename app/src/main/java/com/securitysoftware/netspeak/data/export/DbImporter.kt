package com.securitysoftware.netspeak.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.securitysoftware.netspeak.data.db.DbContract
import com.securitysoftware.netspeak.data.db.NetSpeakDatabase
import org.json.JSONObject

class DbImporter(private val context: Context) {

    private val dbHelper = NetSpeakDatabase(context)

    /**
     * Importa desde SAF (recomendado)
     */
    fun importFromUri(uri: Uri): Boolean {

        val jsonString = context.contentResolver
            .openInputStream(uri)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: return false

        return importFromJsonString(jsonString)
    }

    /**
     * Importación real
     */
    private fun importFromJsonString(jsonString: String): Boolean {

        val db = dbHelper.writableDatabase

        return try {

            val json = JSONObject(jsonString)

            db.beginTransaction()

            // LIMPIAR DB
            db.delete(DbContract.DeviceTable.TABLE, null, null)
            db.delete(DbContract.BranchTable.TABLE, null, null)

            val branches = json.getJSONArray("branches")

            for (i in 0 until branches.length()) {

                val branchObj = branches.getJSONObject(i)

                val branchValues = ContentValues().apply {
                    put(
                        DbContract.BranchTable.NAME,
                        branchObj.getString("name")
                    )
                }

                val branchId = db.insert(
                    DbContract.BranchTable.TABLE,
                    null,
                    branchValues
                )

                val devices = branchObj.getJSONArray("devices")

                for (j in 0 until devices.length()) {

                    val device = devices.getJSONObject(j)

                    val typeId = when (device.getString("type")) {
                        "DVR" -> 1
                        "PANEL" -> 2
                        "ACCESO" -> 3
                        else -> 3
                    }

                    val deviceValues = ContentValues().apply {
                        put(DbContract.DeviceTable.BRANCH_ID, branchId)
                        put(DbContract.DeviceTable.NAME, device.getString("name"))
                        put(DbContract.DeviceTable.IP, device.getString("ip"))
                        put(DbContract.DeviceTable.TYPE_ID, typeId)
                    }

                    db.insert(
                        DbContract.DeviceTable.TABLE,
                        null,
                        deviceValues
                    )
                }
            }

            db.setTransactionSuccessful()
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
        }
    }
}
