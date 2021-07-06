package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.ReflectionUtils
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ReflectionUtilsTests {
    class CreateFromEmptyConstructorTest {
        class OnlyPrivateConstructor private constructor()
        class PublicConstructorWithParams(args: Int)
        abstract class AbstractClass
        class ValidClass_1
        class ValidClass_2 {
            constructor()
            constructor(args: Int)
        }

        private inline fun<reified T> assertThrowsWhen() {
            assertFails {
                ReflectionUtils.createFromEmptyConstructor(T::class.java)
            }
        }

        @Test
        fun throwsWhenOnlyPrivateConstructor() {
            assertThrowsWhen<OnlyPrivateConstructor>()
        }

        @Test
        fun throwsWhenPublicConstructorWithParams() {
            assertThrowsWhen<PublicConstructorWithParams>()
        }

        @Test
        fun throwsWhenAbstractClass() {
            assertThrowsWhen<AbstractClass>()
        }

        @Test
        fun createsValidInstance() {
            fun testCase(c: Class<*>) {
                val instance = ReflectionUtils.createFromEmptyConstructor(c)
                assertEquals(c, instance.javaClass)
            }

            testCase(ValidClass_1::class.java)
            testCase(ValidClass_2::class.java)
        }
    }

    class CreateFromEmptyConstructorOrInstance {
        class OnlyPrivateConstructor_NoInstance private constructor()
        class PublicConstructorWithParams_NoInstance(args: Int)
        abstract class AbstractClass

        class InstanceIsMember_PrivateConstructor private constructor() {
            @JvmField
            val INSTANCE = Any()
        }

        class InstanceHasInvalidType_PrivateConstructor private constructor() {
            companion object {
                @JvmField
                val INSTANCE = Any()
            }
        }

        class InstanceIsNull_PrivateConstructor private constructor() {
            companion object {
                @JvmField
                val INSTANCE: Any? = null
            }
        }

        class ValidClass_1
        class ValidClass_2 {
            constructor()
            constructor(args: Int)
        }
        class ValidClass_3 private constructor() {
            companion object {
                @JvmField
                val INSTANCE = ValidClass_3()
            }
        }

        private inline fun<reified T> assertThrowsWhen() {
            assertFails {
                ReflectionUtils.createFromEmptyConstructorOrInstance(T::class.java)
            }
        }

        @Test
        fun throwsWhenOnlyPrivateConstructor_NoInstance() {
            assertThrowsWhen<OnlyPrivateConstructor_NoInstance>()
        }

        @Test
        fun throwsWhenOnlyPublicConstructorWithParams_NoInstance() {
            assertThrowsWhen<PublicConstructorWithParams_NoInstance>()
        }

        @Test
        fun throwsWhenAbstractClass() {
            assertThrowsWhen<AbstractClass>()
        }

        @Test
        fun throwsWhenInstanceIsMember_PrivateConstructor() {
            assertThrowsWhen<InstanceIsMember_PrivateConstructor>()
        }

        @Test
        fun throwsWhenInstanceIsNull_PrivateConstructor() {
            assertThrowsWhen<InstanceIsNull_PrivateConstructor>()
        }

        @Test
        fun createsValidInstance() {
            fun testCase(c: Class<*>) {
                val instance = ReflectionUtils.createFromEmptyConstructorOrInstance(c)

                assertEquals(c, instance.javaClass)
            }

            testCase(ValidClass_1::class.java)
            testCase(ValidClass_2::class.java)
            testCase(ValidClass_3::class.java)
        }
    }
}