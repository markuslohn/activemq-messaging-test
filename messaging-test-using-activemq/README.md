# messaging-test-using-activemq-artemis

In diesem Projekt wird das Bereitstellen und Konsumieren von Nachrichten aus ActiveMQ getestet. Es werden REST-Services zur Verfügung gestellt, um Nachrichten in Queues zu speichern. Das Konsumieren von Nachrichten erfolgt sofort, nach der Bereitstellung in der Queue.

Dieses Testprojekt basiert auf dem Framework Quarkus (Supersonic Subatomic Java Framework) (um mehr über Quarkus zu erfahren, einfach die Website besuchen: https://quarkus.io/ )



## Applikation bauen

```bash
#> ./mvnw compile
```



## Applikation testen



### Applikation im dev-mode starten

Verbindungsdaten zum ActiveMQ-Server anpassen

Öffnen der Datei ./src/main/resources/application.properties und Verbindungsdaten prüfen bzw. anpassen:

```
quarkus.artemis.url=tcp://localhost:61616
quarkus.artemis.username=artemis
quarkus.artemis.password=artemis
```

Applikation mit folgendem Befehl starten

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.



Wenn die Applikation gestartet ist, können die nachfolgenden curl-Aufrufe genutzt werden, um Testnachrichten in ActiveMQ zu speichern.

### Test mit IdocQueue

Message accepted by all consumers (FirstIdocConsumer, SecondIdocConsumer, ThirdIdocConsumer)

```bash
#> curl -X 'POST' \
  'http://localhost:8080/message/idoc' \
  -H 'accept: */*' \
  -H 'Content-Type: application/xml' \
  -d @./src/test/resources/FUNC_LOC_CREATE02.xml
```

Message accepted only by one consumer (FirstIdocConsumer)

```bash
#> curl -X 'POST' \
  'http://localhost:8080/message/idoc' \
  -H 'accept: */*' \
  -H 'Content-Type: application/xml' \
  -d @./src/test/resources/FUNC_LOC_CHANGE02.xml
```

Beim Ausführen der Requests wird im Logfile detaillierte Infos zur Verarbeitung der Nachrichten erhalten:

```bash
2024-02-21 13:06:53,067 INFO  [com.ese.int.mes.ser.jms.FirstIdocConsumer] (pool-8-thread-1) Received Idoc: 0000000000009329 (FUNC_LOC_CREATE02) from queue IdocQueue::idoc-consumer-1
```



### Test mit ContactQueue

```bash
#> curl -X 'POST' \
  'http://localhost:8080/message/contact' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json'
```



## Related Guides

- Artemis JMS ([guide](https://quarkiverse.github.io/quarkiverse-docs/quarkus-artemis/dev/index.html)): Use JMS APIs to connect to ActiveMQ Artemis via its native protocol
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.



## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
