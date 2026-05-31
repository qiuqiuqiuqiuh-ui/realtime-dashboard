FROM flink:1.18.1-scala_2.12-java17

LABEL maintainer="dashboard-team"
LABEL description="Dashboard Flink Metric Job"

# 复制 Job JAR
COPY target/dashboard-flink-1.0.0.jar /opt/flink/usrlib/dashboard-flink.jar

# 环境变量
ENV KAFKA_SERVERS=kafka:9092
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379
ENV FLINK_PROPERTIES="jobmanager.rpc.address: dashboard-flink-jobmanager"
