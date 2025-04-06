FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR to /app
COPY target/*.jar app.jar

# Copy Flyway SQL migration scripts to container (external to JAR)
COPY target/classes/flyway/db/migration /app/flyway/db/migration

# Create logs directory inside container
RUN mkdir -p /logs

# Set timezone
ENV TZ=Asia/Kolkata
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Kolkata"

# Expose app port
EXPOSE 8080

# Start the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
