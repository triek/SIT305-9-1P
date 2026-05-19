package com.example.a7_1p

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LostFoundDatabaseHelperTest {

    private lateinit var db: LostFoundDatabaseHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.deleteDatabase(LostFoundDatabaseHelper.DATABASE_NAME)
        db = LostFoundDatabaseHelper(context)
    }

    @Test
    fun insertItem_keepsExistingFieldsAndCoordinates() {
        val createdAt = System.currentTimeMillis()
        val item = LostFoundItem(
            type = "Lost",
            name = "Test Wallet",
            phone = "0412345678",
            description = "Leather wallet",
            createdAtMillis = createdAt,
            location = "Campus Library",
            latitude = -37.8401,
            longitude = 144.9467,
            category = "Wallets",
            imageUri = ""
        )

        val id = db.insertItem(item)
        val stored = db.getItemById(id)

        assertNotNull(stored)
        assertEquals("Lost", stored!!.type)
        assertEquals("Test Wallet", stored.name)
        assertEquals("0412345678", stored.phone)
        assertEquals("Leather wallet", stored.description)
        assertEquals(createdAt, stored.createdAtMillis)
        assertEquals("Campus Library", stored.location)
        assertEquals(-37.8401, stored.latitude, 0.000001)
        assertEquals(144.9467, stored.longitude, 0.000001)
        assertEquals("Wallets", stored.category)
    }
}
