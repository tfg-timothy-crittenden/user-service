# ---------- Build from source ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
COPY mvnw.cmd .

RUN mvn -q -DskipTests dependency:go-offline

COPY src src/

RUN mvn -q -DskipTests clean package


# ---------- Common runtime ----------
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

EXPOSE 8082

ENV SERVER_PORT=8082
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://user-db:5432/user_db
ENV SPRING_DATASOURCE_USERNAME=myuser
ENV SPRING_DATASOURCE_PASSWORD=secret

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


# ---------- Local build ----------
FROM runtime AS local

COPY --from=build /app/target/*.jar app.jar


# ---------- CI build ----------
FROM runtime AS ci

COPY target/*.jar app.jar