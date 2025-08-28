FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/fitness-tracker-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
