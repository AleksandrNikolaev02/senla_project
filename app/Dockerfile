# Стадия 0: Сборка библиотеки
FROM maven:3.9.0-eclipse-temurin-19 AS library
WORKDIR /home
COPY common-dto /home/common-dto
RUN mvn -f /home/common-dto/pom.xml clean install

# Стадия 1: Кеширование зависимостей
FROM maven:3.9.0-eclipse-temurin-19 AS dependencies
WORKDIR /home/app
COPY --from=library /root/.m2 /root/.m2
COPY pom.xml /home/app/pom.xml
COPY parent-pom.xml /home/pom.xml
RUN mvn dependency:go-offline

# Стадия 2: Сборка приложения
FROM maven:3.9.0-eclipse-temurin-19 AS build
WORKDIR /home/app
COPY --from=dependencies /root/.m2 /root/.m2
COPY src /home/app/src
RUN mvn clean package -DskipTests  # Пропускаем тесты для ускорения

# Стадия 3: Запуск приложения
FROM openjdk:19-jdk-slim
WORKDIR /app
COPY --from=build /home/app/target/app-0.0.1-SNAPSHOT.jar /usr/local/lib/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
