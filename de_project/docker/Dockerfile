FROM apache/airflow:2.5.0

# Install additional Python packages
COPY requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt

# Set up Spark integration
ENV SPARK_HOME=/opt/spark
ENV PATH=$PATH:$SPARK_HOME/bin

# Install Java (required for Spark)
USER root
RUN apt-get update && apt-get install -y openjdk-11-jdk && apt-get clean

# Create necessary directories
RUN mkdir -p /opt/airflow/models /opt/spark/warehouse
RUN chown -R airflow:airflow /opt/airflow/models /opt/spark/warehouse

USER airflow