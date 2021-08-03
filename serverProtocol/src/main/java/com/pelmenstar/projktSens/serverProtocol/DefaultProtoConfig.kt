package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RawRepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract

object DefaultProtoConfig: ProtoConfig {
    override val port: Int
        get() = 10001

    override val weatherChannelReceiveInterval: Int
        get() = 10000

    override val repoContract: RepoContract
        get() = RawRepoContract
}