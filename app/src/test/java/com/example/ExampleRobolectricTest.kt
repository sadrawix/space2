package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CloneItem
import com.example.data.prefs.PreferencesManager
import com.example.engine.CloneEngineManager
import com.example.engine.EngineType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Multiclonix Space", appName)
    }

    @Test
    fun `clone engine manager selects supported engine`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = CloneEngineManager(context)
        val selection = manager.selectActiveEngine("AUTO")

        assertNotNull(selection.activeEngine)
        assertNotNull(selection.reason)
        assertTrue(selection.reason.isNotBlank())
    }

    @Test
    fun `preferences manager handles space lock and vip toggle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        prefs.setVip(true)
        assertTrue(prefs.isVip.value)

        prefs.setSpaceLock(true, "1234")
        assertTrue(prefs.isSpaceLockEnabled.value)
        assertTrue(prefs.verifyPin("1234"))
        assertFalse(prefs.verifyPin("0000"))

        prefs.setSpaceLock(false, "")
        assertFalse(prefs.isSpaceLockEnabled.value)
    }

    @Test
    fun `clone item entity creation defaults`() {
        val clone = CloneItem(
            packageName = "com.facebook.lite",
            originalAppName = "Lite",
            displayName = "Lite (Clone)",
            engineType = "WORK_PROFILE"
        )

        assertEquals("com.facebook.lite", clone.packageName)
        assertEquals("CLONE", clone.badgeLabel)
        assertEquals("#2ED573", clone.badgeColorHex)
        assertFalse(clone.isLocked)
        assertTrue(clone.notificationsEnabled)
    }
}
