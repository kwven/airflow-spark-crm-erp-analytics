FROM eclipse-temurin:17-jre-jammy

ARG SPARK_VERSION=3.5.8
ARG HADOOP_MAJOR=3

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
        ca-certificates \
        curl \
        procps \
        tini \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_MAJOR}.tgz" \
    | tar -xz -C /opt \
    && ln -s "/opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_MAJOR}" /opt/spark

ENV JAVA_HOME=/opt/java/openjdk
ENV SPARK_HOME=/opt/spark
ENV PATH="${SPARK_HOME}/bin:${JAVA_HOME}/bin:${PATH}"

WORKDIR /opt/spark
ENTRYPOINT ["/usr/bin/tini", "--"]
