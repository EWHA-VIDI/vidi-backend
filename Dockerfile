FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --chown=10001:10001 build/libs/vidi-backend.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
