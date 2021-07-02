package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract

class ProtoConfigImpl(
    override val repoServerPort: Int,
    override val weatherChannelInfoPort: Int,
    override val weatherChannelReceiveInterval: Int,
    override val repoContract: RepoContract
) : ProtoConfig