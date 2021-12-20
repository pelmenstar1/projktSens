package com.pelmenstar.projktSens.jserver.di

import com.pelmenstar.projktSens.jserver.RepoServer
import com.pelmenstar.projktSens.jserver.WeatherMonitor
import com.pelmenstar.projktSens.jserver.logging.LoggerConfig
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import dagger.Component
import java.net.InetAddress

@Component(modules = [AppModule::class])
interface AppComponent {
    fun host(): InetAddress
    fun protoConfig(): ProtoConfig
    fun weatherRepository(): WeatherRepository
    fun weatherProvider(): WeatherInfoProvider
    fun loggerConfig(): LoggerConfig

    fun server(): RepoServer
    fun weatherMonitor(): WeatherMonitor
}