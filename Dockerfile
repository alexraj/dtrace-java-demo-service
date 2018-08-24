FROM java:8-jre

MAINTAINER Alex Jayaraj

WORKDIR /app

ADD dtrace-java-demo-service-0.0.1-SNAPSHOT.jar /app/dtrace-java-demo-service-0.0.1-SNAPSHOT.jar

COPY start.sh /app/start.sh

RUN chmod 755 /app/start.sh

ENTRYPOINT ["/app/start.sh"]