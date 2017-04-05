# Use the glassfish openjdk from docker hub for testing purpose
FROM glassfish/openjdk

MAINTAINER Luis-Manuel Wolff Heredia <luis-manuel.wolff@outlook.com>

# Define build arguments
ARG GLASSFISH_VERSION=4.1.1 
ARG GLASSFISH_PROFILE=web
ARG MYSQL_VERSION=5.1.38

# Download the JAR of the glassfish-embedded and place it to a folder where the executable can find it
RUN wget -O lib/glassfish-embedded.jar http://central.maven.org/maven2/org/glassfish/main/extras/glassfish-embedded-${GLASSFISH_PROFILE}/${GLASSFISH_VERSION}/glassfish-embedded-${GLASSFISH_PROFILE}-${GLASSFISH_VERSION}.jar

# Download the jdbc driver of MySQL database
RUN wget -O lib/mysql-connector-java.jar http://central.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_VERSION}/mysql-connector-java-${MYSQL_VERSION}.jar

# Add the JAR-File that starts the microservice
ADD target/javaee7-microservice.jar .

# Add an example, which is by default shown on start up
ADD deployment/ROOT.war .

# Define environment variables
ENV JAVA_OPS="-Xmx400m -Xms400m" \
    JAVA_ARGS="ROOT.war --contextroot=/" \
    WEB_PORT=8080 \
    DB_HOST=172.17.0.1

# Define the command for start up
CMD java -cp javaee7-microservice.jar:lib/*:${JAVA_HOME}/lib/tools.jar ${JAVA_OPS} com.luiswolff.microservices.Launcher ${JAVA_ARGS}
