package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract

/**
 * Contains constant information of servers
 */
interface ProtoConfig {
    /**
     * Port of repo-server
     */
    val repoServerPort: Int

    /**
     * Port of weatherChannelInfo server
     */
    val weatherChannelInfoPort: Int

    /**
     * Interval of refreshing weather-info and putting it to data repository
     */
    val weatherChannelReceiveInterval: Int

    /**
     * Binary contract of repo-server
     */
    val repoContract: RepoContract
}