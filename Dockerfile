# Stage 1: Build the Java application
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime image with JRE 21, yt-dlp, and ffmpeg
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install runtime dependencies: ffmpeg, python3, curl, ca-certificates
RUN apk add --no-cache ffmpeg python3 curl ca-certificates bash \
    && curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp \
    && yt-dlp --version \
    && ffmpeg -version

# Copy packaged jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8000

ENV PORT=8000 \
    APP_YT_DLP_BINARY_PATH=/usr/local/bin/yt-dlp

ENTRYPOINT ["java", "-jar", "app.jar"]
