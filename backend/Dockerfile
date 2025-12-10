# -------- Build-Stage: JAR bauen --------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# alles ins Image kopieren
COPY . .

# gradlew ausführbar machen (wichtig bei Windows-Repos)
RUN chmod +x ./gradlew

# Spring Boot Jar bauen
RUN ./gradlew bootJar --no-daemon

# -------- Runtime-Stage: schlankes JRE --------
FROM eclipse-temurin:17-jre
WORKDIR /app

# fertiges Jar aus dem Build-Container holen
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
