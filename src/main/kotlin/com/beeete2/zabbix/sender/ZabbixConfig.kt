package com.beeete2.zabbix.sender

import java.util.concurrent.TimeUnit

data class ZabbixConfig(
        val host: String = "127.0.0.1",
        val port: Int = 10051,
        val socketTimeout: Int = TimeUnit.SECONDS.toSeconds(30).toInt(),
        val connectTimeout: Int = TimeUnit.SECONDS.toSeconds(30).toInt()
)
