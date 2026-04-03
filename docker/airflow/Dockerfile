FROM apache/airflow:3.1.8-python3.11

USER root

ARG SPARK_VERSION=3.5.8
ARG HADOOP_MAJOR=3
ARG SBT_VERSION=1.12.3

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        openjdk-17-jre-headless \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL "https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_MAJOR}.tgz" \
    | tar -xz -C /opt \
    && ln -s "/opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_MAJOR}" /opt/spark

RUN curl -fsSL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" \
    | tar -xz -C /opt \
    && ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt

ENV JAVA_HOME_17=/usr/lib/jvm/java-17-openjdk-amd64
ENV JAVA_HOME=${JAVA_HOME_17}
ENV SPARK_HOME=/opt/spark
ENV PATH="${SPARK_HOME}/bin:${JAVA_HOME_17}/bin:${PATH}"

USER airflow

COPY requirements.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt
