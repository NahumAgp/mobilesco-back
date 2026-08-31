# -------- BUILD STAGE --------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests


# -------- RUNTIME STAGE --------
FROM eclipse-temurin:21-jre-alpine

ARG BUILD_REVISION="local"
ARG BUILD_CREATED="unknown"
LABEL org.opencontainers.image.title="mobilesco-backend" \
      org.opencontainers.image.revision="${BUILD_REVISION}" \
      org.opencontainers.image.created="${BUILD_CREATED}"

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
