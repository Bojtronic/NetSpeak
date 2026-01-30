package com.securitysoftware.netspeak.data.repository

import android.content.ContentValues
import android.content.Context
import com.securitysoftware.netspeak.data.db.DbContract
import com.securitysoftware.netspeak.data.db.NetSpeakDatabase
import com.securitysoftware.netspeak.data.model.Branch
import com.securitysoftware.netspeak.data.model.Device
import com.securitysoftware.netspeak.data.model.DeviceType

class BranchRepository(context: Context) {

    private val dbHelper = NetSpeakDatabase(context)

    // =========================
    // ENCONTRAR DISPOSITIVOS POR SUCURSAL
    // =========================
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

    // =========================
    // OBTENER TODAS LAS SUCURSALES
    // =========================
    fun getAllBranches(): List<Branch> {

        val branches = mutableListOf<Branch>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DbContract.BranchTable.TABLE,
            arrayOf(
                DbContract.BranchTable.ID,
                DbContract.BranchTable.NAME
            ),
            null,
            null,
            null,
            null,
            DbContract.BranchTable.NAME
        )

        while (cursor.moveToNext()) {
            branches.add(
                Branch(
                    id = cursor.getInt(0),
                    name = cursor.getString(1)
                )
            )
        }

        cursor.close()
        return branches
    }

    // =========================
    // AGREGAR SUCURSAL
    // =========================
    fun addBranch(name: String): Long {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbContract.BranchTable.NAME, name)
        }

        return db.insert(
            DbContract.BranchTable.TABLE,
            null,
            values
        )
    }

    // =========================
    // ACTUALIZAR SUCURSAL
    // =========================
    fun updateBranch(branchId: Int, newName: String): Int {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbContract.BranchTable.NAME, newName)
        }

        return db.update(
            DbContract.BranchTable.TABLE,
            values,
            "${DbContract.BranchTable.ID} = ?",
            arrayOf(branchId.toString())
        )
    }

    // =========================
    // ELIMINAR SUCURSAL
    // (borra también dispositivos asociados)
    // =========================
    fun deleteBranch(branchId: Int) {

        val db = dbHelper.writableDatabase

        // Primero eliminar dispositivos
        db.delete(
            DbContract.DeviceTable.TABLE,
            "${DbContract.DeviceTable.BRANCH_ID} = ?",
            arrayOf(branchId.toString())
        )

        // Luego la sucursal
        db.delete(
            DbContract.BranchTable.TABLE,
            "${DbContract.BranchTable.ID} = ?",
            arrayOf(branchId.toString())
        )
    }

    fun getDevicesByBranch(branchId: Int): List<Device> {

        val devices = mutableListOf<Device>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT d.${DbContract.DeviceTable.ID},
               d.${DbContract.DeviceTable.NAME},
               d.${DbContract.DeviceTable.IP},
               t.${DbContract.DeviceTypeTable.NAME}
        FROM ${DbContract.DeviceTable.TABLE} d
        JOIN ${DbContract.DeviceTypeTable.TABLE} t
          ON d.${DbContract.DeviceTable.TYPE_ID} = t.${DbContract.DeviceTypeTable.ID}
        WHERE d.${DbContract.DeviceTable.BRANCH_ID} = ?
        """,
            arrayOf(branchId.toString())
        )

        while (cursor.moveToNext()) {
            devices.add(
                Device(
                    id = cursor.getInt(0),
                    branchId = branchId,
                    name = cursor.getString(1),
                    ip = cursor.getString(2),
                    type = cursor.getString(3)
                )
            )
        }

        cursor.close()
        return devices
    }

    fun addDevice(
        branchId: Int,
        name: String,
        ip: String,
        typeId: Int
    ): Long {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbContract.DeviceTable.BRANCH_ID, branchId)
            put(DbContract.DeviceTable.NAME, name)
            put(DbContract.DeviceTable.IP, ip)
            put(DbContract.DeviceTable.TYPE_ID, typeId)
        }

        return db.insert(
            DbContract.DeviceTable.TABLE,
            null,
            values
        )
    }

    fun updateDevice(
        deviceId: Int,
        name: String,
        ip: String,
        typeId: Int
    ): Int {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DbContract.DeviceTable.NAME, name)
            put(DbContract.DeviceTable.IP, ip)
            put(DbContract.DeviceTable.TYPE_ID, typeId)
        }

        return db.update(
            DbContract.DeviceTable.TABLE,
            values,
            "${DbContract.DeviceTable.ID} = ?",
            arrayOf(deviceId.toString())
        )
    }

    fun deleteDevice(deviceId: Int) {

        val db = dbHelper.writableDatabase

        db.delete(
            DbContract.DeviceTable.TABLE,
            "${DbContract.DeviceTable.ID} = ?",
            arrayOf(deviceId.toString())
        )
    }

    // DEVICE TYPES

    fun getAllDeviceTypes(): List<DeviceType> {
        val list = mutableListOf<DeviceType>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DbContract.DeviceTypeTable.TABLE,
            null, null, null, null, null, null
        )

        while (cursor.moveToNext()) {
            list.add(
                DeviceType(
                    id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DbContract.DeviceTypeTable.ID)
                    ),
                    name = cursor.getString(
                        cursor.getColumnIndexOrThrow(DbContract.DeviceTypeTable.NAME)
                    )
                )
            )
        }

        cursor.close()
        return list
    }

    fun addDeviceType(name: String) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DbContract.DeviceTypeTable.NAME, name)
        }
        db.insert(DbContract.DeviceTypeTable.TABLE, null, values)
    }

    fun updateDeviceType(id: Int, name: String) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put(DbContract.DeviceTypeTable.NAME, name)
        }
        db.update(
            DbContract.DeviceTypeTable.TABLE,
            values,
            "${DbContract.DeviceTypeTable.ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun deleteDeviceType(id: Int) {
        val db = dbHelper.writableDatabase
        db.delete(
            DbContract.DeviceTypeTable.TABLE,
            "${DbContract.DeviceTypeTable.ID} = ?",
            arrayOf(id.toString())
        )
    }



}
