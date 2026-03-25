# syntax=docker/dockerfile:1

# Build stage
FROM cgr.dev/chainguard/maven:latest-dev AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests package

# Runtime stage
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
