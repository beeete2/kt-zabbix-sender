package com.beeete2.zabbix.sender

data class ZabbixMetric(
        val host: String,
        val key: String,
        val value: String,
        val clock: Long = System.currentTimeMillis() / 1000L
)
