# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper and pom
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Make mvnw executable (Windows fix)
RUN chmod +x mvnw

# Download dependencies first (better caching)
RUN ./mvnw -B dependency:go-offline

# Copy the rest of the source
COPY src src

# Build the app
RUN ./mvnw -B clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render will override via $PORT)
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
