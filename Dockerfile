FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy everything (source + Maven wrapper + pom.xml)
COPY . .

# Build the app, skip tests, and copy the resulting JAR to /app/app.jar
RUN ./mvnw package -DskipTests && cp target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]