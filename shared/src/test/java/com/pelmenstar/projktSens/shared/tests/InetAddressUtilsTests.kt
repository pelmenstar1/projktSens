package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.InetAddressUtils
import org.junit.Test
import java.net.InetAddress
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InetAddressUtilsTests {
    @Test
    fun parseNumericalIpv4_succeed() {
        fun succeedOn(str: String) {
            val actual = InetAddressUtils.parseNumericalIpv4OrNull(str)
            val expected = InetAddress.getByName(str)

            assertEquals(expected, actual)
        }

        succeedOn("1.2.3.4")
        succeedOn("123.255.124.64")
        succeedOn("1.23.45.100")
        succeedOn("0.0.0.0")
    }

    @Test
    fun parseNumericalIpv4_fails() {
        fun failsOn(str: String) {
            assertNull(InetAddressUtils.parseNumericalIpv4OrNull(str))
        }

        failsOn("some shit")
        failsOn("1000.438.-1.434")
        failsOn("123")
        failsOn("123.234")
        failsOn("1.23.4")
        failsOn("1222.4444.4444.2222")
        failsOn("256.256.256.256")
        failsOn("123.")
        failsOn("123.34.")
        failsOn("222.33.55.")
    }

    @Test
    fun isValidNumericalIpv4_succeed() {
        fun succeedOn(str: String) {
            assertTrue(InetAddressUtils.isValidNumericalIpv4(str))
        }

        succeedOn("1.2.3.4")
        succeedOn("123.255.124.64")
        succeedOn("1.23.45.100")
        succeedOn("0.0.0.0")
    }

    @Test
    fun isValidNumericalIpv4_fails() {
        fun failsOn(str: String) {
            assertFalse(InetAddressUtils.isValidNumericalIpv4(str))
        }

        failsOn("some shit")
        failsOn("1000.438.-1.434")
        failsOn("123")
        failsOn("123.234")
        failsOn("1.23.4")
        failsOn("1222.4444.4444.2222")
        failsOn("256.256.256.256")
        failsOn("123.")
        failsOn("123.34.")
        failsOn("222.33.55.")
    }
}