package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RawRepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RepoRequest
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import kotlin.test.assertEquals

class RawRepoContractTests {
    @Test
    fun repoRequest_no_args() {
        val random = Random(0)

        repeat(10) {
            val request = RepoRequest(random.nextInt(255))

            repoRequestReadWrite(request)
        }
    }

    @Test
    fun repoRequest_with_args() {
        val random = Random(0)

        repeat(10) {
            val args = random.nextBytes(64)
            val request = RepoRequest(random.nextInt(255), TestObject(args))

            repoRequestReadWrite(request)
        }
    }

    @Test
    fun repoResponse_empty() {
        repoResponseReadWrite<Any>(RepoResponse.Empty)
    }

    @Test
    fun repoResponse_error() {
        val random = Random(0)

        repeat(10) {
            val response = RepoResponse.error(random.nextInt())

            repoResponseReadWrite<Any>(response)
        }
    }

    @Test
    fun repoResponse_ok_lessThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(RawRepoContract.RESPONSE_BUFFER_SIZE / 2))
        val response = RepoResponse.ok(obj)

        repoResponseReadWrite<TestObject>(response)
    }

    @Test
    fun repoResponse_ok_greaterThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(RawRepoContract.RESPONSE_BUFFER_SIZE * 2))
        val response = RepoResponse.ok(obj)

        repoResponseReadWrite<TestObject>(response)
    }

    private fun repoRequestReadWrite(request: RepoRequest) {
        runBlocking {
            val output = ByteArrayOutputStream()
            RawRepoContract.writeRequest(request, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val readFromInput = RawRepoContract.readRequest(input)

            assertEquals(request, readFromInput)
        }
    }

    private inline fun<reified T:Any> repoResponseReadWrite(response: RepoResponse) {
        runBlocking {
            val output = ByteArrayOutputStream()
            RawRepoContract.writeResponse(response, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val readFromInput = RawRepoContract.readResponse(input, T::class.java)

            assertEquals(response, readFromInput)
        }
    }

    private class TestObject(private val rawData: ByteArray) {
        companion object {
            @Suppress("unused")
            @JvmField
            val SERIALIZER = object : ObjectSerializer<TestObject> {
                override fun getSerializedObjectSize(value: TestObject): Int {
                    return value.rawData.size + 4
                }

                override fun writeObject(value: TestObject, writer: ValueWriter) {
                    writer.emitInt32(value.rawData.size)
                    writer.emitByteArray(value.rawData)
                }

                override fun readObject(reader: ValueReader): TestObject {
                    val size = reader.readInt32()
                    val rawData = reader.readByteArray(size)

                    return TestObject(rawData)
                }
            }
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o -> rawData contentEquals o.rawData }
        }

        override fun hashCode(): Int {
            return rawData.contentHashCode()
        }
    }
}