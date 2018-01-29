package com.beeete2.zabbix.sender

import com.alibaba.fastjson.JSONObject
import com.natpryce.konfig.*
import io.github.hengyunabc.zabbix.api.DefaultZabbixApi
import io.github.hengyunabc.zabbix.api.RequestBuilder
import io.github.hengyunabc.zabbix.api.ZabbixApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ZabbixSenderTests {

    lateinit var config: Configuration

    val hostname = "test"

    @Before
    fun setUp() {
        config = EnvironmentVariables() overriding
                ConfigurationProperties.fromResource("defaults.properties")
        setUpZabbix()
    }

    @Test
    fun send() {
        val metrics = listOf(
                ZabbixMetric(host = hostname, key = "cpu", value = "3.2"),
                ZabbixMetric(host = hostname, key = "memory", value = "336", clock = System.currentTimeMillis() / 1000L)
        )
        val zabbixRequest = ZabbixRequest(metrics = metrics)
        val zabbixConfig = ZabbixConfig(host = config[zabbix.server])
        val zabbixSender = ZabbixSender(zabbixConfig)
        val zabbixResponse = zabbixSender.send(zabbixRequest)
        assertThat(zabbixResponse.hasError).isEqualTo(false)
        assertThat(zabbixResponse.response).isEqualTo("success")
        assertThat(zabbixResponse.info).startsWith("processed: 2; failed: 0; total: 2;")
    }

    private fun setUpZabbix(web: String = this.config[zabbix.web], hostname: String = this.hostname) {
        val url = "http://$web/api_jsonrpc.php"
        val zabbixApi = DefaultZabbixApi(url)
        zabbixApi.init()

        // login to Zabbix
        val login = zabbixApi.login("Admin", "zabbix")
        assertThat(login).isEqualTo(true)

        if (hasHost(zabbixApi, hostname)) {
            return
        }

        // create test zabbixServer
        val hostid = createHost(zabbixApi, hostname)

        // create items
        createItem(zabbixApi, hostid, "cpu")
        createItem(zabbixApi, hostid, "memory")

        println("has been created zabbixServer and items for testing.")

        Thread.sleep(TimeUnit.MINUTES.toMillis(1L))
    }

    private fun hasHost(zabbixApi: ZabbixApi, hostname: String): Boolean {
        val filter = JSONObject()
        filter.put("host", hostname)

        val request = RequestBuilder.newBuilder()
                .method("host.get")
                .paramEntry("filter", filter)
                .build()

        val response = zabbixApi.call(request)
        return response.getJSONArray("result").isNotEmpty()
    }

    private fun createItem(zabbixApi: ZabbixApi, hostid: String, name: String): String {
        val request = RequestBuilder.newBuilder()
                .method("item.create")
                .paramEntry("name", name)
                .paramEntry("key_", name)
                .paramEntry("hostid", hostid)
                .paramEntry("type", "2") // Zabbix Trapper
                .paramEntry("value_type", "0") // Numeric
                .build()

        val response = zabbixApi.call(request)
        val item = response.getJSONObject("result").getJSONArray("itemids").getString(0)
        return item
    }

    private fun createHost(zabbixApi: ZabbixApi, hostname: String): String {
        val agentInterface = JSONObject()
        agentInterface.put("type", "1")
        agentInterface.put("main", "1")
        agentInterface.put("useip", "1")
        agentInterface.put("ip", "127.0.0.1")
        agentInterface.put("dns", "")
        agentInterface.put("port", "10050")

        val group = JSONObject()
        group.put("groupid", "2")

        val request = RequestBuilder.newBuilder()
                .method("host.create")
                .paramEntry("host", hostname)
                .paramEntry("interfaces", agentInterface)
                .paramEntry("groups", group)
                .build()

        val response = zabbixApi.call(request)
        val hostId = response.getJSONObject("result").getJSONArray("hostids").getString(0)
        return hostId
    }

    object zabbix : PropertyGroup() {
        val server by stringType
        val web by stringType
    }
}
