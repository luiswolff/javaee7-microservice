# Use the glassfish openjdk from docker hub for testing purpose
FROM glassfish/openjdk

MAINTAINER Luis-Manuel Wolff Heredia <luis-manuel.wolff@outlook.com>

# Add glassfish dependency
ADD lib/glassfish-embedded-web.jar lib/glassfish-embedded-web.jar

# Add mysql dependency
ADD lib/mysql-connector-java.jar lib/mysql-connector-java.jar

# Add the JAR-File that starts the microservice
ADD javaee7-microservice.jar .

# Add an example, which is by default shown on start up
ADD example.war .

# Define environment variables
ENV JAVA_OPS="-Xmx400m -Xms400m" \
    JAVA_ARGS="example.war --contextroot=/" \
    WEB_PORT=8080 \
    DB_CONF=true \
    DB_HOST=172.17.0.1

# Expors the port to listen on
EXPOSE ${WEB_PORT}

# Define the command for start up
CMD java -cp javaee7-microservice.jar:lib/*:${JAVA_HOME}/lib/tools.jar ${JAVA_OPS} com.luiswolff.microservices.Launcher ${JAVA_ARGS}
