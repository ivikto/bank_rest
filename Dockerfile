FROM openjdk:21-jdk-slim
LABEL authors="ivikto"

WORKDIR /app
COPY ../target/bankcards-*.jar bankcards.jar
EXPOSE 8080
EXPOSE 5005