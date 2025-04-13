FROM openjdk:19-jdk-slim

WORKDIR /app

COPY /target/email_service-0.0.1-SNAPSHOT.jar /app/email_service.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/email_service.jar"]
