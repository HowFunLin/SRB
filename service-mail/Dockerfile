FROM mayan31370/openjdk-alpine-with-chinese-timezone:8-jdk
VOLUME /tmp
COPY ./target/service-mail-0.0.1-SNAPSHOT.jar service-mail.jar
ENTRYPOINT ["java", "-jar", "service-mail.jar"]
EXPOSE 8120