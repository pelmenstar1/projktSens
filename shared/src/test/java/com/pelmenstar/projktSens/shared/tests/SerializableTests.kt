package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertFails

class SerializableTests {
    @Test
    fun throws_if_serializer_is_not_public() {
        throwsIf<SerializerIsNotPublic>()
    }

    @Test
    fun throws_if_serializer_is_not_static() {
        throwsIf<SerializerIsNotStatic>()
    }

    @Test
    fun throws_if_serializer_is_not_final() {
        throwsIf<SerializerIsNotFinal>()
    }

    @Test
    fun throws_if_serializer_is_null() {
        throwsIf<SerializerIsNull>()
    }

    @Test
    fun throws_if_serializer_is_not_instance_of_ObjectSerializer() {
        throwsIf<SerializerIsNotInstanceOfObjectSerializer>()
    }

    private inline fun<reified T:Any> throwsIf() {
        assertFails { Serializable.getSerializer(T::class.java) }
    }

    @Test
    fun returns_serializer_if_meet_requirements() {
        val serializer = Serializable.getSerializer(TestClass_FitRequirements::class.java)

        Assert.assertSame(serializer, TestClass_FitRequirements.SERIALIZER)
    }

    class SerializerIsNotPublic {
        companion object {
            private val SERIALIZER = EmptySerializer<SerializerIsNotPublic>()
        }
    }

    class SerializerIsNotStatic {
        @JvmField
        val SERIALIZER = EmptySerializer<SerializerIsNotPublic>()
    }

    class SerializerIsNotFinal {
        companion object {
            @JvmField
            var SERIALIZER = EmptySerializer<SerializerIsNotPublic>()
        }
    }

    class SerializerIsNull {
        companion object {
            @JvmField
            val SERIALIZER: ObjectSerializer<SerializerIsNull>? = null
        }
    }

    class SerializerIsNotInstanceOfObjectSerializer {
        companion object {
            @JvmField
            val SERIALIZER = Any()
        }
    }

    class TestClass_FitRequirements {
        companion object {
            @JvmField
            val SERIALIZER = EmptySerializer<TestClass_FitRequirements>()
        }
    }

    class EmptySerializer<T>: ObjectSerializer<T> {
        override fun getSerializedObjectSize(value: T): Int = throw NotImplementedError()
        override fun writeObject(value: T, writer: ValueWriter) = throw NotImplementedError()
        override fun readObject(reader: ValueReader): T = throw NotImplementedError()
    }
}