FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="dashboard-team"
LABEL description="Dashboard Collector Service"

WORKDIR /app

COPY target/dashboard-consumer-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=100", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
