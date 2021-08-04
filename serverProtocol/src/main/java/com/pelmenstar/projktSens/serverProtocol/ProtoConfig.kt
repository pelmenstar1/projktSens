package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import java.net.InetSocketAddress

/**
 * Contains constant information of servers
 */
interface ProtoConfig {
    val socketAddress: InetSocketAddress

    /**
     * Interval of refreshing weather-info and putting it to data repository
     */
    val weatherChannelReceiveInterval: Int

    /**
     * Binary contract of repo-server
     */
    val repoContract: RepoContract
}