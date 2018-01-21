package com.beeete2.zabbix.sender

data class ZabbixResponse(
        val response: String = "",
        val info: String = "",
        val hasError: Boolean = false
)
