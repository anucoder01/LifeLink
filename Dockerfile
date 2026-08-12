FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Give execution rights
RUN chmod +x ./mvnw
# Build all dependencies for offline use
RUN ./mvnw dependency:go-offline -B
# Copy the project source
COPY src src
# Package the application
RUN ./mvnw package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
# Create non-root user
RUN useradd -m appuser
USER appuser
# Copy the built jar from the build stage
COPY --from=build /app/target/lifelink-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
