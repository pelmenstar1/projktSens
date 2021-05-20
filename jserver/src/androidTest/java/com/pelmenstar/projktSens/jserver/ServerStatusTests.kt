package com.pelmenstar.projktSens.jserver

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pelmenstar.projktSens.serverProtocol.DefaultProtoConfig
import com.pelmenstar.projktSens.serverProtocol.ServerStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerStatusTests {
    @Test
    fun must_return_available_status_when_status_server_is_running() {
        serverConfig = TestConfig(InstrumentationRegistry.getInstrumentation().context)

        val statusServer = StatusServer()
        statusServer.start()
        Thread.sleep(5000)

        val status: ServerStatus

        runBlocking {
            status = ServerAvailabilityProvider(DefaultProtoConfig).getStatus()
        }

        Assert.assertEquals(ServerStatus.AVAILABLE, status)
        statusServer.stop()
    }

    @Test
    fun must_return_not_available_status_if_status_server_is_not_running() {
        val status: ServerStatus

        runBlocking {
            status = ServerAvailabilityProvider(DefaultProtoConfig).getStatus()
        }

        Assert.assertEquals(ServerStatus.NOT_AVAILABLE, status)
    }
}