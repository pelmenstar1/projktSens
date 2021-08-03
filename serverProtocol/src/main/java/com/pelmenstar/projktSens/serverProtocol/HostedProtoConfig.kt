package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import java.net.InetAddress
import java.net.InetSocketAddress

class HostedProtoConfig(val host: InetAddress, private val config: ProtoConfig): ProtoConfig {
    val socketAddress = InetSocketAddress(host, config.port)

    override val port: Int get() = config.port
    override val weatherChannelReceiveInterval: Int get() = config.weatherChannelReceiveInterval
    override val repoContract: RepoContract get() = config.repoContract
}