FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="dashboard-team"
LABEL description="Dashboard Gateway Service"

WORKDIR /app

COPY target/dashboard-gateway-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=100", \
    "-XX:+PrintGCDetails", \
    "-Xloggc:/app/gc.log", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
