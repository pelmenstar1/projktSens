package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import java.net.InetSocketAddress

class ProtoConfigImpl(
    override val socketAddress: InetSocketAddress,
    override val weatherChannelReceiveInterval: Int,
    override val repoContract: RepoContract
) : ProtoConfig