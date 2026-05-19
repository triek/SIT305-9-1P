package com.example.a7_1p.data

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LostFoundDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_ITEMS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_ITEMS ADD COLUMN $COLUMN_LATITUDE REAL NOT NULL DEFAULT 0.0")
            db.execSQL("ALTER TABLE $TABLE_ITEMS ADD COLUMN $COLUMN_LONGITUDE REAL NOT NULL DEFAULT 0.0")
        }
    }

    fun insertItem(item: LostFoundItem): Long {
        val values = ContentValues().apply {
            put(COLUMN_TYPE, item.type)
            put(COLUMN_NAME, item.name)
            put(COLUMN_PHONE, item.phone)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_CREATED_AT, item.createdAtMillis)
            put(COLUMN_LOCATION, item.location)
            put(COLUMN_LATITUDE, item.latitude)
            put(COLUMN_LONGITUDE, item.longitude)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_IMAGE_URI, item.imageUri)
        }

        return writableDatabase.insert(TABLE_ITEMS, null, values)
    }


    fun insertItems(sampleItems: List<LostFoundItem>) {
        writableDatabase.beginTransaction()
        try {
            sampleItems.forEach { insertItem(it) }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun clearAllItems(): Int {
        return writableDatabase.delete(TABLE_ITEMS, null, null)
    }

    fun getAllItems(): List<LostFoundItem> {
        val items = mutableListOf<LostFoundItem>()
        val query = "SELECT * FROM $TABLE_ITEMS ORDER BY $COLUMN_ID DESC"

        readableDatabase.rawQuery(query, null).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val typeIndex = cursor.getColumnIndexOrThrow(COLUMN_TYPE)
            val nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NAME)
            val phoneIndex = cursor.getColumnIndexOrThrow(COLUMN_PHONE)
            val descriptionIndex = cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)
            val createdAtIndex = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)
            val locationIndex = cursor.getColumnIndexOrThrow(COLUMN_LOCATION)
            val latitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)
            val longitudeIndex = cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)
            val categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)
            val imageUriIndex = cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)

            while (cursor.moveToNext()) {
                items.add(
                    LostFoundItem(
                        id = cursor.getLong(idIndex),
                        type = cursor.getString(typeIndex),
                        name = cursor.getString(nameIndex),
                        phone = cursor.getString(phoneIndex),
                        description = cursor.getString(descriptionIndex),
                        createdAtMillis = cursor.getLong(createdAtIndex),
                        location = cursor.getString(locationIndex),
                        latitude = cursor.getDouble(latitudeIndex),
                        longitude = cursor.getDouble(longitudeIndex),
                        category = cursor.getString(categoryIndex),
                        imageUri = cursor.getString(imageUriIndex)
                    )
                )
            }
        }

        return items
    }

    fun getItemById(id: Long): LostFoundItem? {
        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        readableDatabase.query(
            TABLE_ITEMS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null

            return LostFoundItem(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                createdAtMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
                location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI))
            )
        }
    }

    fun deleteItem(id: Long): Int {
        return writableDatabase.delete(
            TABLE_ITEMS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    companion object {
        const val DATABASE_NAME = "lost_found.db"
        const val DATABASE_VERSION = 3

        const val TABLE_ITEMS = "items"
        const val COLUMN_ID = "id"
        const val COLUMN_TYPE = "type"
        const val COLUMN_NAME = "name"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMAGE_URI = "image_uri"

        private const val CREATE_TABLE_ITEMS = """
            CREATE TABLE $TABLE_ITEMS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_LOCATION TEXT NOT NULL,
                $COLUMN_LATITUDE REAL NOT NULL,
                $COLUMN_LONGITUDE REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_IMAGE_URI TEXT NOT NULL
            )
        """
    }
}
