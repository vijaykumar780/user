# Start with a lightweight Java image
FROM amazoncorretto:17

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file to the container
COPY target/*.war user.war

# Expose the port your app runs on
EXPOSE 8080

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "user.war"]
