FROM mayan31370/openjdk-alpine-with-chinese-timezone:8-jdk
VOLUME /tmp
COPY ./target/service-core-0.0.1-SNAPSHOT.jar service-core.jar
COPY ./src/main/resources/application.yml /config/application.yml
ENTRYPOINT ["java", "-jar", "service-core.jar", "--spring.config.location=/config/application.yml"]
EXPOSE 8110