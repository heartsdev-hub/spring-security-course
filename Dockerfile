FROM maven:3.8.5-openjdk-17-slim
WORKDIR /app
COPY target/course-spring-security-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]