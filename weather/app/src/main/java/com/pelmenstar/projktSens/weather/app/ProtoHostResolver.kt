package com.pelmenstar.projktSens.weather.app

import java.net.InetAddress

interface ProtoHostResolver {
    fun getHost(): InetAddress
}