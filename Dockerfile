FROM maven:3.8.5-openjdk-17-slim
WORKDIR /app
COPY target/course-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 7070
ENTRYPOINT ["java", "-jar", "app.jar"]
