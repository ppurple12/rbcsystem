FROM amazoncorretto:22-alpine

# Create a directory for the application
WORKDIR /app

# Copy the application JAR and native libraries
COPY target/demo-0.0.1-SNAPSHOT.jar /app/myapp.jar
# Copy the native libraries
COPY src/main/resources/ortools-linux-x86-64 /app/ortools-linux-x86-64

# Define the entrypoint for the container with increased heap space
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-XX:+UseG1GC", "-jar", "/app/myapp.jar"]