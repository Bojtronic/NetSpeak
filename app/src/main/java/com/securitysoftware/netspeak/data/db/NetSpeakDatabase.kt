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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.DeviceTable.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.DeviceTypeTable.TABLE}")
        db.execSQL("DROP TABLE IF EXISTS ${DbContract.BranchTable.TABLE}")
        onCreate(db)
    }

}
