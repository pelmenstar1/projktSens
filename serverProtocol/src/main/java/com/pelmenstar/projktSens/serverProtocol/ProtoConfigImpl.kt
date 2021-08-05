package com.pelmenstar.projktSens.serverProtocol

import java.net.InetSocketAddress

class ProtoConfigImpl(
    override val socketAddress: InetSocketAddress,
    override val weatherChannelReceiveInterval: Int,
    override val contract: Contract
) : ProtoConfig