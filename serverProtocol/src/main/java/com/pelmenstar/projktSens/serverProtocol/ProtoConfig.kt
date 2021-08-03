package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract

/**
 * Contains constant information of servers
 */
interface ProtoConfig {
    val port: Int

    /**
     * Interval of refreshing weather-info and putting it to data repository
     */
    val weatherChannelReceiveInterval: Int

    /**
     * Binary contract of repo-server
     */
    val repoContract: RepoContract
}