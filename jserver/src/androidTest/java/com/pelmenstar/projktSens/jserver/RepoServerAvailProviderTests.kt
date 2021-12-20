package com.pelmenstar.projktSens.jserver

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pelmenstar.projktSens.jserver.di.DaggerAppComponent
import com.pelmenstar.projktSens.serverProtocol.ServerAvailabilityProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RepoServerAvailProviderTests {
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val component = DaggerAppComponent.builder().appModule(TestAppModule(context)).build()
    private val server = component.repoServer()

    @Test
    fun isAvailableTest() {
        fun testCase(forceBlocking: Boolean) {
            runBlocking {
                val provider = ServerAvailabilityProvider(component.protoConfig(), forceBlocking)

                server.stop()
                assertFalse(provider.isAvailable(), "forceBlocking=$forceBlocking")

                server.startOnNewThread()
                delay(3000) // wait until server actually starts

                assertTrue(provider.isAvailable(), "forceBlocking=$forceBlocking")

                server.stop()
            }
        }

        testCase(forceBlocking = true)

        // There's no need to test async way if SDK int < 26
        if(Build.VERSION.SDK_INT >= 26) {
            testCase(forceBlocking = false)
        }
    }
}