FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy everything (source + Maven wrapper + pom.xml)
COPY . .

# Make the Maven wrapper executable, then build the app
RUN chmod +x mvnw && ./mvnw package -DskipTests && cp target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
