package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RawRepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import java.net.InetAddress

object DefaultProtoConfig: ProtoConfig {
    private val _serverIp: InetAddress = InetAddress.getByAddress(byteArrayOf(
        192.toByte(),
        168.toByte(),
        17.toByte(),
        21.toByte()
    ))

    override val serverIp: InetAddress
        get() = _serverIp

    override val repoServerPort: Int
        get() = 10001
    override val weatherChannelInfoPort: Int
        get() = 10002
    override val serverStatusPort: Int
        get() = 10003

    override val weatherChannelReceiveInterval: Int
        get() = 10000
    override val repoContract: RepoContract
        get() = RawRepoContract
}