FROM amazoncorretto:21-alpine
WORKDIR /app
COPY target/fitness-tracker-exe.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
