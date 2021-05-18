package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RawRepoContract
import java.net.InetAddress
import java.net.InetSocketAddress
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Contains constant information of servers
 */
interface ProtoConfig {
    /**
     * IP address of server
     */
    val serverIp: InetAddress

    /**
     * Port of repo-server
     */
    val repoServerPort: Int

    /**
     * Port of weatherChannelInfo server
     */
    val weatherChannelInfoPort: Int

    /**
     * Port of server status server
     */
    val serverStatusPort: Int

    /**
     * Interval of refreshing weather-info and putting it to data repository
     */
    val weatherChannelReceiveInterval: Int

    /**
     * Binary contract of repo-server
     */
    val repoContract: RepoContract
}

/**
 * Creates [InetSocketAddress] from [ProtoConfig.serverIp] and [port]. It's the same as:
 * ```
 * val address = InetSocketAddress(config.serverIp, config.*port*)
 * ```
 * But with this method code will be more readable and short
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ProtoConfig.socketAddress(port: Int): InetSocketAddress {
    return InetSocketAddress(serverIp, port)
}

/**
 * Creates [InetSocketAddress] from [ProtoConfig.serverIp] and [port].
 * There is slight difference between `socketAddress(port: Int)` and `socketAddress(port: T.() -> Int)`.
 * Code like:
 * ```
 * val address = config.socketAddress { *some port* }
 * ```
 * will be shorter than:
 * ```
 * val address = config.socketAddress(config.*port*)
 * ```.
 * Because of lambda changes this scope to receiver, and code can be shorter:
 * ```
 * val address = config.socketAddress { repoServerPort }
 * ```
 */
inline fun<T:ProtoConfig> T.socketAddress(port: T.() -> Int): InetSocketAddress {
    contract {
        callsInPlace(port, InvocationKind.EXACTLY_ONCE)
    }

    return InetSocketAddress(serverIp, port())
}