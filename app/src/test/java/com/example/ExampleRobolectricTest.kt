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
    fun `preferences manager supports double-hashed legacy PIN and auto-migrates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        // Simulate the double-hash bug: hashPin(PinHasher.hash("7890"))
        val standardHash = com.example.plan.PinHasher.hash("7890")
        val doubleHashed = prefs.hashPin(standardHash)

        // Store directly in SharedPreferences as if written by the previous buggy version
        context.getSharedPreferences("multiclonix_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("key_space_lock", true)
            .putString("key_space_pin", doubleHashed)
            .commit()

        val reloadedPrefs = PreferencesManager(context)
        assertEquals(doubleHashed, reloadedPrefs.spacePin.value)

        // Verifying with correct PIN "7890" must succeed
        assertTrue(reloadedPrefs.verifyPin("7890"))
        // Incorrect PIN must fail
        assertFalse(reloadedPrefs.verifyPin("1111"))

        // Must auto-migrate to standard single salted hash
        assertEquals(standardHash, reloadedPrefs.spacePin.value)
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

    @Test
    fun `isolated sandbox manager creates protected directory structure with shared_prefs and databases`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = com.example.engine.IsolatedSandboxManager.createIsolatedInstance(
            context = context,
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            instanceIndex = 2
        )

        assertTrue(result.isSuccess)
        val clone = result.getOrNull()
        assertNotNull(clone)
        assertTrue(clone!!.sandboxUuid.isNotBlank())
        assertTrue(clone.sandboxPath.isNotBlank())
        assertEquals("WhatsApp 2", clone.displayName)

        val sandboxDir = java.io.File(clone.sandboxPath)
        assertTrue(sandboxDir.exists())
        assertTrue(sandboxDir.isDirectory)

        val sharedPrefsDir = java.io.File(sandboxDir, "shared_prefs")
        val databasesDir = java.io.File(sandboxDir, "databases")
        val filesDir = java.io.File(sandboxDir, "files")

        assertTrue("shared_prefs directory must exist", sharedPrefsDir.exists())
        assertTrue("databases directory must exist", databasesDir.exists())
        assertTrue("files directory must exist", filesDir.exists())

        // Cleanup
        com.example.engine.IsolatedSandboxManager.deleteIsolatedInstance(context, clone.sandboxUuid, clone.sandboxPath)
        assertFalse(sandboxDir.exists())
    }
}
