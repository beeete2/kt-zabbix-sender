package com.beeete2.zabbix.sender

import com.fasterxml.jackson.annotation.JsonProperty

data class ZabbixRequest(
        val request: String = "sender data",
        val clock: Long = System.currentTimeMillis() / 1000L,
        @get:JsonProperty("data")
        val metrics: List<ZabbixMetric>
)
