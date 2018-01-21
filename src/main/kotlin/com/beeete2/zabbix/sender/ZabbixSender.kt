package com.beeete2.zabbix.sender

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.InetSocketAddress
import java.net.Socket


class ZabbixSender(
        private val zabbixConfig: ZabbixConfig = ZabbixConfig(),
        private val objectMapper: ObjectMapper = ObjectMapper(),
        private val zabbixHeader: ByteArray = byteArrayOf(
                'Z'.toByte(),
                'B'.toByte(),
                'X'.toByte(),
                'D'.toByte(),
                '\u0001'.toByte())
) {

    fun send(zabbixRequest: ZabbixRequest): ZabbixResponse {
        Socket().use { socket ->
            socket.soTimeout = zabbixConfig.socketTimeout
            socket.connect(InetSocketAddress(zabbixConfig.host, zabbixConfig.port), zabbixConfig.connectTimeout)

            socket.getOutputStream().use { output ->
                val bytes = toBytes(zabbixRequest)
                output.write(bytes)
                output.flush()

                socket.getInputStream().use { input ->
                    val buffer = input.readBytes()
                    if (buffer.size < 13) {
                        return ZabbixResponse(hasError = true)
                    }

                    // 13 = header(5) + datalen(8)
                    val jsonString = String(buffer, 13, buffer.size - 13)
                    val zabbixResponse = objectMapper.readValue(jsonString, ZabbixResponse::class.java)
                    return zabbixResponse
                }
            }
        }
    }

    fun toBytes(zabbixRequest: ZabbixRequest): ByteArray {
        val data = objectMapper.writeValueAsBytes(zabbixRequest)
        val datalen = getDataLen(data)
        val bytes = zabbixHeader + datalen + data
        return bytes
    }

    fun getDataLen(data: ByteArray): ByteArray {
        val length = data.size
        val datalen = byteArrayOf(
                (length and 0xFF).toByte(),
                ((length shr 8) and 0x00FF).toByte(),
                ((length shr 16) and 0x0000FF).toByte(),
                ((length shr 24) and 0x000000FF).toByte(),
                '\u0000'.toByte(),
                '\u0000'.toByte(),
                '\u0000'.toByte(),
                '\u0000'.toByte()
        )
        return datalen
    }

}