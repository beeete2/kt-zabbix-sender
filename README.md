# kt-zabbix-sender

Zabbix Sender for kotlin 

Implement Zabbix Sender for Java example 
https://www.zabbix.org/wiki/Docs/protocols/zabbix_sender/1.8/java_example

Example.

```kotlin
val metrics = listOf<ZabbixMetric>(
    ZabbixMetric(host = "yourhost", key = "cpu", value = "2.9"),
    ZabbixMetric(host = "yourhost", key = "memory", value = "333", clock = System.currentTimeMillis() / 1000L)
)
        
val zabbixRequest = ZabbixRequest(metrics = metrics)
val zabbixConfig = ZabbixConfig(host = "127.0.0.1")
val zabbixSender = ZabbixSender(zabbixConfig)
val zabbixResponse = zabbixSender.send(zabbixRequest)
```

