FROM mayan31370/openjdk-alpine-with-chinese-timezone:8-jdk
VOLUME /tmp
COPY ./target/service-gateway-0.0.1-SNAPSHOT.jar service-gateway.jar
ENTRYPOINT ["java", "-jar", "service-gateway.jar"]
EXPOSE 80