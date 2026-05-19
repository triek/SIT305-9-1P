package com.example.a7_1p

import com.example.a7_1p.data.LostFoundDatabaseHelper
import com.example.a7_1p.data.LostFoundItem
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.a7_1p", appContext.packageName)
    }

    @Test
    fun insertReadDeleteItem_worksCorrectly() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(LostFoundDatabaseHelper.DATABASE_NAME)

        val dbHelper = LostFoundDatabaseHelper(context)
        val item = LostFoundItem(
            type = "Lost",
            name = "Alex",
            phone = "0400000000",
            description = "Black wallet",
            date = "2026-05-07",
            location = "Library",
            category = "Accessories",
            imageUri = "content://images/wallet"
        )

        val insertedId = dbHelper.insertItem(item)
        assertTrue(insertedId > 0)

        val fetched = dbHelper.getItemById(insertedId)
        assertNotNull(fetched)
        assertEquals("Alex", fetched?.name)

        val allItems = dbHelper.getAllItems()
        assertEquals(1, allItems.size)

        val deletedRows = dbHelper.deleteItem(insertedId)
        assertEquals(1, deletedRows)
        assertNull(dbHelper.getItemById(insertedId))

        dbHelper.close()
    }

    @Test
    fun dataPersistsAfterReopeningDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(LostFoundDatabaseHelper.DATABASE_NAME)

        val firstHelper = LostFoundDatabaseHelper(context)
        val insertedId = firstHelper.insertItem(
            LostFoundItem(
                type = "Found",
                name = "Jamie",
                phone = "0411111111",
                description = "Silver keychain",
                date = "2026-05-07",
                location = "Cafeteria",
                category = "Keys",
                imageUri = "content://images/keys"
            )
        )
        firstHelper.close()

        val secondHelper = LostFoundDatabaseHelper(context)
        val fetched = secondHelper.getItemById(insertedId)

        assertNotNull(fetched)
        assertEquals("Jamie", fetched?.name)

        secondHelper.deleteItem(insertedId)
        secondHelper.close()
    }
}
