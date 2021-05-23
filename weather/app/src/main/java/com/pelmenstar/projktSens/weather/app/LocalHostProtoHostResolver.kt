package com.pelmenstar.projktSens.weather.app

import java.net.InetAddress

object LocalHostProtoHostResolver: ProtoHostResolver {
    override fun getHost(): InetAddress = InetAddress.getLocalHost()
}