# ---------- Build stage ----------
# Matches <java.version>25</java.version> in pom.xml - a lower-version JDK
# here cannot compile targeting a higher release, which was the build failure.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B


# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

VOLUME ["/app/uploads"]

# Render (and most PaaS platforms) inject a PORT env var at runtime and
# expect the app to listen on it. The shell form of ENTRYPOINT lets ${PORT}
# actually get substituted at container start - the JSON-array exec form
# does NOT do variable expansion. Locally, with no PORT set, it falls back
# to 9090 to match your current application.properties.
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-9090}"]