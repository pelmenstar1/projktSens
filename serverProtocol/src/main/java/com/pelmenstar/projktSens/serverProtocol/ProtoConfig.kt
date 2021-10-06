package com.pelmenstar.projktSens.serverProtocol

import java.net.InetSocketAddress

class ProtoConfig(
    val socketAddress: InetSocketAddress,
    val weatherChannelReceiveInterval: Int,
    val contract: Contract
)