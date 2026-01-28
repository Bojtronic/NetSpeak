package com.securitysoftware.netspeak.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NetSpeakDatabase(context: Context) :
    SQLiteOpenHelper(
        context,
        DbContract.DATABASE_NAME,
        null,
        DbContract.DATABASE_VERSION
    ) {

    override fun onCreate(db: SQLiteDatabase) {

        // Branches
        db.execSQL("""
            CREATE TABLE ${DbContract.BranchTable.TABLE} (
                ${DbContract.BranchTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${DbContract.BranchTable.NAME} TEXT NOT NULL
            )
        """)

        // Device Types
        db.execSQL("""
            CREATE TABLE ${DbContract.DeviceTypeTable.TABLE} (
                ${DbContract.DeviceTypeTable.ID} INTEGER PRIMARY KEY,
                ${DbContract.DeviceTypeTable.NAME} TEXT NOT NULL
            )
        """)

        // Devices
        db.execSQL("""
            CREATE TABLE ${DbContract.DeviceTable.TABLE} (
                ${DbContract.DeviceTable.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${DbContract.DeviceTable.BRANCH_ID} INTEGER,
                ${DbContract.DeviceTable.TYPE_ID} INTEGER,
                ${DbContract.DeviceTable.NAME} TEXT,
                ${DbContract.DeviceTable.IP} TEXT
            )
        """)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.DeviceTable.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.DeviceTypeTable.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.BranchTable.TABLE}")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {

        // Device Types
        db.execSQL("INSERT INTO device_types VALUES (1, 'DVR')")
        db.execSQL("INSERT INTO device_types VALUES (2, 'PANEL')")
        db.execSQL("INSERT INTO device_types VALUES (3, 'ACCESS')")

        // Branch
        db.execSQL("INSERT INTO branches (name) VALUES ('Sucursal 1')")

        // Devices
        db.execSQL("""
            INSERT INTO devices (branch_id, type_id, name, ip) VALUES
            (1, 1, 'Grabador 1', '192.168.100.1'),
            (1, 1, 'Grabador 2', '192.168.100.5'),
            (1, 2, 'Panel Alarma', '192.168.100.3'),
            (1, 3, 'Acceso 1', '192.168.100.8'),
            (1, 3, 'Acceso 2', '192.168.100.9'),
            (1, 3, 'Acceso 3', '192.168.100.10')
        """)
    }
}
