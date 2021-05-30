package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import java.net.InetAddress
import java.net.InetSocketAddress

class HostedProtoConfig(val host: InetAddress, private val config: ProtoConfig): ProtoConfig {
    override val repoServerPort: Int get() = config.repoServerPort
    override val weatherChannelInfoPort: Int get() = config.weatherChannelInfoPort
    override val weatherChannelReceiveInterval: Int get() = config.weatherChannelReceiveInterval
    override val repoContract: RepoContract get() = config.repoContract

    fun socketAddress(port: Int): InetSocketAddress {
        return InetSocketAddress(host, port)
    }

    inline fun socketAddress(port: HostedProtoConfig.() -> Int): InetSocketAddress {
        return InetSocketAddress(host, port())
    }
}