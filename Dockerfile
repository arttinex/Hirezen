# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy the Maven wrapper and pom first - this layer only rebuilds when
# dependencies change, so normal code edits reuse Docker's build cache.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy source and build the jar.
COPY src ./src
RUN ./mvnw clean package -DskipTests -B


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user rather than root, standard container hardening.
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Uploaded profile photos and resumes are written to ./uploads at runtime
# (see WebConfig / ProfileService) - mount a volume here so they survive
# container restarts and rebuilds instead of vanishing with the container.
VOLUME ["/app/uploads"]

# Matches the port seen in your logs (Tomcat on 9090). Change this and the
# server.port in application.properties together if you use a different port.
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
