package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.lofitown.model.BuildingType
import com.example.lofitown.model.TimeOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lofi Town", appName)
    }

    @Test
    fun `building types and upgrades calculate vibes correctly`() {
        val coffeeShop = BuildingType.COFFEE_SHOP
        val level1Vibes = coffeeShop.vibesPerSecForLevel(1)
        val level2Vibes = coffeeShop.vibesPerSecForLevel(2)

        assertTrue(level2Vibes > level1Vibes)
        assertEquals(coffeeShop.cost, 15L)
    }

    @Test
    fun `time of day transitions correctly from minutes`() {
        val morning = TimeOfDay.fromMinutes(7 * 60)
        val afternoon = TimeOfDay.fromMinutes(12 * 60)
        val sunset = TimeOfDay.fromMinutes(18 * 60)
        val midnight = TimeOfDay.fromMinutes(23 * 60)

        assertEquals(TimeOfDay.DAWN, morning)
        assertEquals(TimeOfDay.DAY, afternoon)
        assertEquals(TimeOfDay.DUSK, sunset)
        assertEquals(TimeOfDay.NIGHT, midnight)
    }
}
