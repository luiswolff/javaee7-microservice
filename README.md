
Java-EE-7-Microservice
================================================

Bei diesem Projekt handelt es sich um eine Vorlage für Microservices, welche auf Java EE 7 aufbauen sollen.
Es stellt den Anwendungsserver [GlassFish][1] in einer Minimal-Konfiguration zu Verfügung.
Ziel des Projekts ist es eine Plattform zu schaffen, mit der Java EE-Archive (z.B. WAR oder EAR) effizient in [Docker][2]-Containern betrieben werden können.
Dies bedeutet, dass zum Betreiben von Anwendungen keine für einzelne Anwendungsserver spezifische Konfiguration durchgeführt werden soll.
Stattdessen soll der Server sich selbst konfigurieren.
Dies ist eine Vorraussetzung für den erfolgreichen Betrieb von Microservices mit Java EE.

Dem steht die Annahme zu Grunde, dass eine Microservices-Architektur möglichst Technologieunabhängig sein sollte, damit einzelne Services möglichst Ersetzbar bleiben.
Um dies wirklich erreichen zu können darf der Betrieb der Service keine technologiespezifischen Eigenschaften aufweisen.
Die generischte Möglichkeit auf das Verhalten eines Systemprozesses Einfluss zu nehmen ist das setzen von Umgebungsvariablen.
Damit dies funktioniert muss der Prozess aber so implementiert werden, dass er auf vordefinierte Umgebungsvariablen reagieren kann und sein Verhalten entsprechend anpasst.
Das hier vorgestellte Projekt setzt genau diesen Ansatz um, in dem ein Java-Programm geschrieben wurde, welches unter verwendung von Umgebungsvariablen einen [Embedded GlassFish-Server][21] konfiguiert und started.

Umsetzung
---------

Da der Build und Start von Docker-Container sehr stark von System-Variablen beeinflusst werden kann muss auch der Startvorgang des Java-EE-Containers auf diese Mechanismen reagieren können.
Zu diesem Zweck wurde dieses Programm so geschrieben, dass es den Embedded GlassFish über folgende System-Varaiablen konfigurieren kann:

**Netzwerk-Dienst**

| Variable     | Standard | Beschreibung |
| :-------     | :------: |:------------ |
| WEB_LISTENER | HTTP     | Legt fest, über welches Protokoll der  Server erreicht werden kann. Werte können sein HTTP (Standard) oder HTTPS. |
| WEB_PORT     | 8080     | Legt den Netzwerkport fest über den der Dienst erreichbar ist |

*Hinweis*: Bisher wurde die Verbindung über HTTPS nicht getestet. 
Ist dies eine Anforderung, müsste zusätzlich geprüft werden, wie GlassFish ein bestimmtes Zertifikat und Key-Store verwenden kann.

**Datenbank**

| Variable     | Standard               | Beschreibung |
| :-------     | :------:               | :----------- |
| DB_CLASS     | (...).MysqlDataSource  | Java-Class, welche eine Verbindung zu einer Datenbank herstellen kann |
| DB_TYPE      | javax.sql.DataSource   | Interface, welches von der Datenbank-Klasse bereit gestellt wird |
| DB_USER      | sa                     | Benutzer, mit dem die Anwendung sich auf der Datenbank einloggen soll |
| DB_PASSWD    | sa                     | Password, mit dem sich die Anwendung gegenüber der Datenbank authentifizieren soll |
| DB_NAME      | test                   | Name der Datenbank (bei MySQL auch Schema genannt) zu der eine Verbindung aufgebaut werden soll |
| DB_HOST      | localhost              | Host-Name des Datenbank-Servers |
| DB_PORT      | 3306                   | Port auf dem der Datenbank-Dienst angeboten wird. Standard ist der MySQL-Port |
| DB_POOL      | MySQLPool              | Name des Pools, welcher für die Verwaltung der Verbindungen zu Datenbank verantwortlich ist |
| DB_JNDI      | jdbc/__default         | Name über den die zu deployende Anwendung die Datenbank-Verbindung aufrufen soll | 

*Anmerkung:* Je nach Bedarf können noch weitere Konfigurationen für Mail und JMS folgen.

Build
-----

Als Build-Werkzeug verwendet dieses Project [Apache Maven][3]. 
Das Projekt kann über folgenden Kommandozeilen-Befehl gebaut werden:

````
$ mvn clean package
````

Das Resultat ist die Datei `target/javaee7-microservice.jar` und ein zusätzlicher Ordern `lib` in dem die Abhängigkeiten automatisch abgelegt wurden.

*Anmerkung:* Theoretisch ist es auch möglich eine sogenannte **Fat-JAR** (auch **Uber-JAR** genannt) zu erzeugen.
 Dies hätte den Vorteil, dass die erzeugte JAR leichter von der Commandozeile aus verwendet werden kann, da sich alle Abhängigkeiten bereits in der JAR befinden.
 Maven kann dies beispielsweise mit dem [Maven-Assembly-Plugin][6] umsetzen.
 Allerdings können folgende **Probleme mit Fat-JAR** auftreten:
  - Die Buildzeit dauert, abhängig von der Größe der Dependencies, länger, da diese alle komplett in die neue JAR kopiert werden müssen.
    Die Jar für den GlassFish-Server Embedded Web in der Version 4.1.1 ist beispielsweise fast *53,3 MB* groß.
  - Das Schichtsystem beim Bauen von Docker-Containern kann nicht mehr optimal genutzt werden.
    Immer wenn dieses Projekt geändert werden würde, müssten auch alle Abhängigkeiten, welche sich in der Regel nicht geändert haben, dem Docker-Image neu hinzugefügt werden.
    
 Auf die hier genannten Probleme ist [Adem Bien][7] in einem [Vortrag auf der W-JAX16][8] bereits eingegangen.
 Dennoch könnte die Möglichkeit Fat-JAR's zu bauen nachgereicht werden, sollte dies eine Anforderung sein.

Abhängigkeiten
--------------

Das Projekt an sich besitzt zwei direkte Abhängigkeiten, welche über Maven bezogen werden:

**Runtime Dependencies**
 - **[GlassFish-Embedded-Web (Version 4.1.1)][9]:** Uber-Jar, welche das komplette Web-Profile des GlassFish-Server zu Verfügung stellt.
   Dies erlaubt es der Anwendung Java-Web-Archive (WAR) auszuführen.
 - **[MySQL-Connector (Version 5.1.38)][10]:** Erlaubt es Verbindungen zu einer MySQL-Datenbanken bis zur Version 5.7 herzustellen.

**Platform**
 - **[Java SE Development Kit 8][13]:** Alle Java EE Server benötigen ein JDK, da sie selbst zur Laufzeit Byte-Code generieren müssen.
   Das Projekt selber benutzt Sprach-Elemente von Java 8, wie beispielsweise Lambda-Ausdrücke.
   
**Transitive Dependencies**
 - **tools.jar:** Ein JDK-Tool, welches von GlassFish verwendet wird, um JSP-Seiten compilieren zu können.
   Da dies zwecks kompatibilität immer vom lokalen JDK bezogen werden sollte, wird diese nicht mit im `lib`-Ordner aufgenommen.
   Stattdessen wird sie dem GlassFish über des setzen eines Classpath-Eintrags zu Verfügung gestellt.

*Überlegung:* Um auch beim Build flexibler mit unterschiedlichen Server-Profilen (All, Web, Nucleus etc.: siehe [hier][11])umgehen zu können, könnten sogenannte [Maven Build Profiles][12] eingesetzt werden.
Damit könnte die Dependencies für ein bestimmtes GlassFish-Profile für unterschiedliche Laufzeit-Szenarien konfiguriert werden. 

Starten
-------

Da es sich bei Java-EE-7-Microservices um ein Java-Projekt handelt, muss es auch mit dem Programm `java` bzw. `javaw` (Achtung: JDK benutzen!) gestartet werden.
Auf Grund der Tatsache der GlassFish von bestimmten JDK-Tools abhängig ist, wird empfohlen das Programm durch das verwenden von Klassenpfaden zu starten.
Es muss beachtet werden, dass nach der Spezifikation der Java-Anwendung (siehe [für Unix][14] oder [für Windows][15]) ein Setzen des Klassenpfad bei der Verwendung des `-jar`-Parameter ignoriert wird.
Daher wurde auf eine ausführbare JAR verzichtet.
Das Projekt sollte wie folgt gestartet werden: 
   
      ````
      $ java -cp javaee7-microservice.jar;lib/*;${JAVA_HOME}/lib/tools.jar ${JAVA_OPS} com.luiswolff.microservice.Launcher example.war --contextroot=/
      ````
 
 Als Java-Argument muss mindestens ein relativer oder absoluter Pfad zum Deployment-Archive übergeben werden.
 Sollte dieses Argument nicht gesetzt sein, oder die angegebene Datei nicht existieren, bricht das Programm mit den Fehler-Code 1 ab.
 Desweiteren können zusätzliche GlassFish-Spezifische Deployment-Parameter übergeben werden.
 Mögliche Parameter können dem [GlassFish 4 Reference Manual][16] ab Seite 1-313 entnommen werden.
 
Deployment
----------

Um ein Deployment auf einer Docker-Engine zu vereinfachen, wurde diesem Projekt ein Dockerfile hinzugefügt.
Ein Image kann mit folgenden Befehlen gebaut werden:

````
$ mvn clean package && docker build -t luiswolff/glassfish-ms:1.0.0.BETA-web
````

Das Dockerfile baut ein Docker-Image indem es zunächst die für die Ausführung geforderte Datei-Struktur herstellt.
Des weiteren wird das Beispiel-Deployment `example.war` dem Dateisystem hinzugefügt.
Die Umgebung wird so konfiguriert, dass die Anwendung auf den Standardport für Anwendungsserver 8080 startet.
Als Datenbank-Host soll der Hostrechner der Docker-Engine verwendet werden.
Bei den [Standard-Docker-Netzwerkeinstellungen][18] ist dieser unter der IP-Adresse 172.17.0.1 erreichbar.

Wenn der Container gestartet wird, führt er den im Abschnitt Start gezeigten Befehl aus.
Dadurch können dem Image auch weiter Archive (z.B. JDBC-Treibern) hinzugefügt werden.
   
Die Anwendung Java ist verfügbar, weil das hier erstellte Image von Image `glassfish/openjdk` ([Siehe Docker-Hub][19]) abgeleitet wird.
Von diesem wird auch das offizelle [GlassFish-Server-Image][20] (Verwendet die Standalone variante des Server) abgeleitet.

Wichtiger ist allerdings die Tatsache, dass das Docker-Image zwei neue Umgebungsvariablen definiert.
Diese sind hauptsächlich für Anwendungsentwickler interessant und sollten möglichst auf die Bedürfnisse der tatsächlich zu deployenden Anwendung angepasst werden.

| Variable  | Standard                      | Beschreibung |
| :-------  | :------:                      |:------------ |
| JAVA_OPS  | "-Xmx400m -Xms400m"           | Optionen, welche der JVM mitgegeben werden können. Für mögliche Werte siehe [Java Spezifikation für Unix][14].|
| JAVA_ARGS | "example.war --contextroot=/" | Pfad zum Deploymentartefact und deployment parameter. Für mögliche Werte siehe [GlassFish Reference Manual][16].|

Der obigen Tabelle kann entnommen werden, dass der GlassFish-Server ohne weitere Konfiguration immer 400 MB Speicher reservieren wird.
Ferner wird er versuchen die Beispielanwendung, welche im Dateisystem des Image abgelegt wurde, zu deployen.
Diese wird unter dem Root-Context bereit gestellt.
Hieraus ergibt sich, dass Anwendungsentwickler prinzipell zwei Möglichkeiten haben, mit denen sie das Image erweitern können, um eigene Anwendungen bereitzustellen:

1. Sie legen ihr Archive mit den Namen `example.war` im Dateisystem des Containers ab und überschreiben somit die Beispielanwendung.
Damit wird die Anwendung unter dem Root-Context `http[s]://<host>:<port>/` bereitgestellt. 
Dies könnte allerdings im Microservice-Umfeld zu Problemen führen.
Ein Frontendserver müsste so konfiguriert werden, dass er die intern von Service verwendeten URL in externe umwandelt, da verschiedene Services auch unter unterschiedlichen URL-Pfaden erreichbar sein sollten.
Ein Dockerfile könnte nun wie folgt aussehen:

   **Dockerfile für Apps, die unter dem Root-Context erreichbar sein sollen: `http[s]://<host>:<port>/`**
   ````
   FROM luiswolff/glassfish-ms:1.0.0.BETA
   
   ADD target/myapp.war ROOT.war
   ````

2. Die einfachere Variante ist wohl die zu deployende Anwendung mit dem Namen abzulegen unter dem sie auch erreichbar sein soll und entsprechend die Umgebungsvariable `${JAVA_ARGS}` anzupassen.
Soll beispielsweise eine Anwendung unter dem Context `http[s]://<host>:<port>/myapp` erreichbar sein, sollte das Artifakt dem Image mit `ADD target/<myapp>.war myapp.war` hinzugefügt werden.
Die Umgebung sollte ebenfalls mit `ENV JAVA_ARGS="myapp.war"` angepasst werden.
Da GlassFish Java Web Archive standardmäßig unter den Dateinamen bereit stellt, ist keine weitere Konfiguration nötig.
Ein Dockerfile könnte hier wie folgt aussehen:

    **Dockerfile für Apps, die unter einen bestimmten Pfad erreichbar sein sollen: `http[s]://<host>:<port>/myapp`**
    ````
    FROM luiswolff/glassfish-ms:1.0.0.BETA
    
    ENV JAVA_ARGS="myapp.war"
    
    ADD target/myapp.war .
    ````

Autor und Datum
----------------

Luis-Manuel Wolff Heredia  
Email: [luis-manuel.wolff@outlook.com](mailto:luis-manuel.wolff@outlook.com])  
Datum: 13.03.2017

[1]: https://glassfish.java.net
[2]: https://www.docker.com
[3]: https://maven.apache.org
[4]: https://maven.apache.org/plugins/maven-jar-plugin/
[5]: https://docs.oracle.com/javase/8/docs/technotes/guides/jar/jar.html#JAR_Manifest
[6]: https://maven.apache.org/plugins/maven-assembly-plugin/
[7]: http://about.adam-bien.com/
[8]: https://vimeo.com/190996051
[9]: https://glassfish.java.net/docs/4.0/embedded-server-guide.pdf
[10]: https://dev.mysql.com/doc/connector-j/5.1/en/
[11]: http://central.maven.org/maven2/org/glassfish/main/extras/
[12]: http://maven.apache.org/guides/introduction/introduction-to-profiles.html
[13]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[14]: http://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html
[15]: http://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html
[16]: https://glassfish.java.net/docs/4.0/reference-manual.pdf
[18]: https://docs.docker.com/engine/userguide/networking/
[19]: https://hub.docker.com/r/glassfish/openjdk/
[20]: https://hub.docker.com/r/glassfish/server/
[21]: https://embedded-glassfish.java.net/
