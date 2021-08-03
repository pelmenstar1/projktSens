package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract

class ProtoConfigImpl(
    override val port: Int,
    override val weatherChannelReceiveInterval: Int,
    override val repoContract: RepoContract
) : ProtoConfig