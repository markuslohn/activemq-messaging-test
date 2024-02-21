# ActiveMQ-Test

Im Rahmen eines Kundenprojektes haben sich Fragen hinsichtlich Filter- und Anpassungsmöglichkeiten bei ActiveMQ ergeben. Nachfolgend habe ich die Installations- und Konfigurationsschritte dargestellt, die ich im Rahmen der Analyse durchgeführt habe. Zunächst habe ich als Infrastruktur Mulitpass und Docker verwendet. Später bin ich auf Podman umgestiegen. Ich habe jeweils die Kommandos für beide Infrastrukturen aufgeführt.

**Hinweis:** Alle Schritte habe ich auf einem MacBook Pro mit M2 Prozessor ausgeführt. Möglicherweise sind einige Schritte aber auf einer anderen Hardware nicht erforderlich!



## Benutzung

Zunächst müssen die Schritte der Installation und Konfiguration von ActiveMQ durchlaufen werden (siehe unten). Danach können die Tests mit ActiveMQ durchgeführt werden. Hierzu gibt es zwei Projekte, die jeweils auch eine Anleitung beinhalten:

- [message-transformer](./message-transformer/README.md)
- [messaging-test-using-activemq](./messaging-test-using-activemq/README.md)



## Installation ActiveMQ

Installation auf MacBook Pro mit M2 Prozesser und Multipass.io durchgeführt (siehe VM-Konfiguration: https://github.com/markuslohn/multipass-java-integration-dev). Leider funktionieren die offiziellen ActiveMQ Docker Images auf dem M2-Prozessor nicht. Aus diesem Grund war es notwendig, das Docker-Image selbst für die Ziel-Prozessor-Architektur neu zu erzeugen.

Für die Installation waren folgende Hinweise sehr hilfreich:
- [Offizielle Dokumentation zum Bauen eines eigenen Docker-Images für ActiveMQ](https://activemq.apache.org/components/artemis/documentation/)
- [ActiveMQ Artemis: Easy Docker Creation also for ARM](https://www.dontesta.it/en/2021/02/15/activemq-artemis-easy-docker-creation-also-for-arm/)

Folgende Schritte durchgeführt:

1. Repository gekloned

	```bash
	#> git clone --depth 1 https://github.com/apache/activemq-artemis.git
	```

2. Sourcen kompiliert

	```bash
	#> cd activemq-artemis
	#> mvn clean install -DskipTests
	```

​	**Hinweis:** 
​	Beim Ausführen habe ich immer Fehler im Projekt tests/e2e-tests erhalten. Habe das Modul e2e-tests dann in der pom.xml deaktiviert, so dass der Build dann ohne Fehler durchgeführt wurde. Kann eventl. mit dem Einbinden des externen Folders in die VM zusammenhängen. Nicht weiter analysiert.

3. Vorbereitung Docker File

  ```bash
  #> cd artemis-docker
  #> ./prepare-docker.sh --from-local-dist --local-dist-path ../artemis-distribution/target/apache-artemis-2.32.0-SNAPSHOT-bin/apache-artemis-2.32.0-SNAPSHOT/
  ```

  Nach erfolgreicher Ausführung des Befehls steht in der Ausgabe, welche Schritte als nächstes auszuführen sind.

4. Docker Image für ARM erzeugen:

   **Hinweis Allgemein:** Das Docker Image wird nur lokal installiert und nicht im docker-hub zur Verfügung gestellt!

   <u>Docker:</u>	

   ```bash
   #> cd ../artemis-distribution/target/apache-artemis-2.32.0-SNAPSHOT-bin/apache-artemis-2.32.0-SNAPSHOT/
   #> docker buildx build --platform linux/arm64 -t mlohn/activemq-artemis  -f ./docker/Dockerfile-ubuntu-11 .
   ```

   <u>Podman:</u>	

```bash
	#> cd ../artemis-distribution/target/apache-artemis-2.32.0-SNAPSHOT-bin/apache-artemis-2.32.0-SNAPSHOT/
	#> podman build -t activemq-artemis --platform="linux/arm64" -f ./docker/Dockerfile-ubuntu-11 .
```

​	**Hinweis Podman:** Die Zeile `COPY ./docker/docker-run.sh /` muss nach `COPY ./docker/docker-run.sh /docker-run.sh` und die Zeile `ADD . /opt/activemq-artemis`muss nach `ADD $PWD /opt/activemq-artemis` in der Datei Dockerfile-ubuntu-11 angepasst werden.

5. Docker Container starten

​	<u>Docker:</u>

```bash
	#> docker run --detach --name myqueuecontainer  -p 61616:61616 -p 8161:8161 mlohn/activemq-artemis
```

​	<u>Podman:</u>
```bash
	#> podman run --interactive --tty -d --name activemq  -p 61616:61616 -p 8161:8161 activemq-artemis
```

6. Management Console im Webbrowser aufrufen

	Mit der URL http://localhost:8161/ kann die Console im Browser geöffnet werden:
	
	Anmeldedaten: artemis/artemis




## Konfiguration ActiveMQ

Command Line Interface starten

<u>Docker:</u>

```bash
#> docker exec -it activemq ./bin/artemis shell
```

<u>Podman:</u>

```bash
#> podman exec -it activemq ./bin/artemis shell
```

Login zu ActiveMQ als Administrator:

```bash
connect --user=artemis --password=artemis --url tcp://localhost:61616
```



### Addressen und Queues anlegen

```bash
address create --name=ContactQueue --anycast=false --multicast=true
queue create --name=contact-consumer-1 --address=ContactQueue --anycast=false --durable=true --purge-on-no-consumers=false --auto-create-address=false

address create --name=IdocQueue --anycast=false --multicast=true
queue create --name=idoc-consumer-1 --address=IdocQueue --anycast=false --durable=true --purge-on-no-consumers=false --auto-create-address=false
queue create --name=idoc-consumer-2 --address=IdocQueue --anycast=false --durable=true --purge-on-no-consumers=false --auto-create-address=false --filter="XPATH '//IDOC/EDI_DC40/MESTYP/text()=\'FUNC_LOC_CREATE\''"
queue create --name=idoc-consumer-3 --address=IdocQueue --anycast=false --durable=true --purge-on-no-consumers=false --auto-create-address=false
```

Alternative für Filterung nach JMSType:

```bash
queue create --name=idoc-consumer-2 --address=IdocQueue --anycast=false --durable=true --purge-on-no-consumers=false --auto-create-address=false --filter="JMSType='FUNC_LOC_CREATE02'"
```



### Filter für eine Queue anlegen

Neue Queue mit XPATH filter anlegen:
```bash
queue create --name=idoc-consumer-3 --address=IdocQueue --anycast=false --durable=true --purge-on-no-consumers=false --auto-create-address=false --filter="XPATH '//IDOC/EDI_DC40/MESTYP/text()=FUNC_LOC_CREATE'"
```
oder

```bash
 queue update --multicast --address=IdocQueue --name=idoc-consumer-2 --filter="XPATH '//IDOC/EDI_DC40/MESTYP/text()=FUNC_LOC_CREATE'"
```
**=>HINWEIS** Update setzt den Filter nicht! Auch ein Neustart des Brokers schaffte keine Abhilfe. Das Update-Kommando wird aber ohne Fehler ausgeführt. Der Filter scheint nur bei Neuanlage der Queue akzeptiert zu werden.



## Anhang

### vi im Docker-Container nachinstallieren

```bash
#> docker exec -u 0 -it 18c000e201ef /bin/bash
```
Innerhalb des Containers dann noch folgende Befehle ausführen:
```bash
#> apt-get update
#> apt-get install vim
```



