package com.securitysoftware.netspeak.data.repository

import android.content.ContentValues
import android.content.Context
import com.securitysoftware.netspeak.data.db.DbContract
import com.securitysoftware.netspeak.data.db.NetSpeakDatabase
import com.securitysoftware.netspeak.data.model.Branch
import com.securitysoftware.netspeak.data.model.Device

class BranchRepository(context: Context) {

    private val dbHelper = NetSpeakDatabase(context)

    // =========================
    // EXISTENTE (NO TOCADO)
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
    // NUEVO – OBTENER TODAS LAS SUCURSALES
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
    // NUEVO – AGREGAR SUCURSAL
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
    // NUEVO – ACTUALIZAR SUCURSAL
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
    // NUEVO – ELIMINAR SUCURSAL
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
}
