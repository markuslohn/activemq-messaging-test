# Messaging-transformer

Mit einem message-transformer kann eine Nachricht direkt in ActiveMQ bearbeitet werden. In diesem Projekt wird exemplarisch ein message-transformer definiert, der lediglich eine Logausgabe enthält, die eigentliche Nachricht aber nicht verändert. 

In diesem Fall wird lediglich der Konfigurationsaspekt mit ActiveMQ getestet.



## Konfiguration

1. JAR-File für den message-transformer erstellen

```bash
#> ./mvnw package
```



2. JAR-File des message-transformer im Classpath von ActiveMQ bereitstellen

```bash
#> docker cp message-transformer-1.0.0-SNAPSHOT.jar activemq:/var/lib/artemis-instance/lib
```

**Hinweis:** Ein Transformer kann eigentlich nur in Verbindung mit einem [diverts](https://activemq.apache.org/components/artemis/documentation/latest/diverts.html#diverting-and-splitting-message-flows) or [core bridges](https://activemq.apache.org/components/artemis/documentation/latest/core-bridges.html#core-bridges) verwendet werden. Es besteht wohl keine Möglichkeit einen Transformer generell für eine Queue zu aktivieren.



3. Message-Transformer in broker.xml bekanntgeben

```xml
<divert name="prices-divert">
  <address>priceUpdates</address>   
  <forwarding-address>priceForwarding</forwarding-address>   
  <filter string="office='New York'"/>   
  <transformer-class-name>com.esentri.integration.examples.activemq.IdocMessageTransformer </transformer-class-name>  
  <exclusive>true</exclusive>
</divert>
```

4. Neustart von ActiveMQ erforderlich, damit der message-transformer aktiv werden kann.

