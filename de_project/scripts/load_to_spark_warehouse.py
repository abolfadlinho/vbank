import pandas as pd
import logging
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, current_timestamp
from airflow.exceptions import AirflowException
import os

logger = logging.getLogger(__name__)

def load_to_spark_task(**context):
    """
    Load cleaned and transformed data into Apache Spark data warehouse.
    """
    try:
        # Get input path from transformation task
        ti = context['task_instance']
        input_path = ti.xcom_pull(task_ids='transform_data', key='output_path')
        
        if not input_path:
            raise AirflowException("No input path received from transformation task")
        
        # Initialize Spark session
        spark = create_spark_session()
        
        # Load transformed data
        df_spark = spark.read.parquet(input_path)
        logger.info(f"Loading {df_spark.count()} records to Spark warehouse")
        
        # Add warehouse metadata
        df_spark = df_spark.withColumn("warehouse_load_timestamp", current_timestamp())
        
        # Configure warehouse path
        warehouse_path = get_warehouse_config()['transactions_table_path']
        
        # Perform idempotent load using partition overwrite strategy
        # Partition by date for efficient querying and updates
        df_spark = df_spark.withColumn("partition_date", 
                                     col("timestamp").cast("date"))
        
        logger.info(f"Writing data to warehouse path: {warehouse_path}")
        
        # Write with partition overwrite mode for idempotency
        (df_spark.write
         .mode("overwrite")
         .partitionBy("partition_date")
         .option("path", warehouse_path)
         .saveAsTable("fraud_detection.transactions"))
        
        # Verify the load
        loaded_count = spark.sql("SELECT COUNT(*) FROM fraud_detection.transactions").collect()[0][0]
        logger.info(f"Successfully loaded {loaded_count} total records to warehouse")
        
        # Store metadata for downstream tasks
        ti.xcom_push(key='warehouse_records', value=loaded_count)
        ti.xcom_push(key='warehouse_table', value='fraud_detection.transactions')
        
        spark.stop()
        return warehouse_path
        
    except Exception as e:
        logger.error(f"Error in Spark load task: {str(e)}")
        raise AirflowException(f"Spark warehouse load failed: {str(e)}")

def create_spark_session():
    """Create and configure Spark session."""
    spark = (SparkSession.builder
             .appName("FraudDetectionETL")
             .config("spark.sql.adaptive.enabled", "true")
             .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
             .config("spark.sql.warehouse.dir", "/opt/spark/warehouse")
             .enableHiveSupport()
             .getOrCreate())
    
    # Create database if not exists
    spark.sql("CREATE DATABASE IF NOT EXISTS fraud_detection")
    
    return spark

def get_warehouse_config():
    """Get warehouse configuration."""
    return {
        'transactions_table_path': '/opt/spark/warehouse/fraud_detection/transactions',
        'models_path': '/opt/spark/warehouse/fraud_detection/models'
    }
