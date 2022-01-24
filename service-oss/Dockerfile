FROM mayan31370/openjdk-alpine-with-chinese-timezone:8-jdk
VOLUME /tmp
COPY ./target/service-oss-0.0.1-SNAPSHOT.jar service-oss.jar
ENTRYPOINT ["java", "-jar", "service-oss.jar"]
EXPOSE 8130