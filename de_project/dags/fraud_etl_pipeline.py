from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.operators.bash import BashOperator
from airflow.utils.dates import days_ago
import sys
import os

# Add the project root to Python path
sys.path.append("/opt/airflow/dags")

# Import task functions
from scripts.extract_transactions import extract_transactions_task
from scripts.validate_extraction_data import validate_extraction_task
from scripts.transform_data import transform_data_task
from scripts.validate_transformed_data import validate_transformed_task
from scripts.load_to_spark_warehouse import load_to_spark_task
from scripts.train_ai_model import train_ai_model_task

# Default arguments for the DAG
default_args = {
    "owner": "data-engineering-team",
    "depends_on_past": False,
    "start_date": days_ago(1),
    "email_on_failure": True,
    "email_on_retry": False,
    "retries": 2,
    "retry_delay": timedelta(minutes=5),
    "catchup": False,
}

# Define the DAG
dag = DAG(
    "fraud_detection_etl_pipeline",
    default_args=default_args,
    description="Fraud Detection ETL Pipeline with ML Training",
    schedule_interval="@daily",  # Run daily
    max_active_runs=1,  # Prevent overlapping runs
    tags=["fraud-detection", "etl", "ml"],
)

# Task 1: Extract transactions from PostgreSQL
extract_task = PythonOperator(
    task_id="extract_transactions",
    python_callable=extract_transactions_task,
    dag=dag,
    doc_md="""
    ## Extract Transactions Task
    
    Extracts new or updated transaction data from PostgreSQL database using
    incremental loading based on last_extraction_timestamp.
    """,
)

# Task 2: Validate extracted data
validate_extraction = PythonOperator(
    task_id="validate_extraction_data",
    python_callable=validate_extraction_task,
    dag=dag,
    doc_md="""
    ## Validate Extraction Data Task
    
    Performs initial data quality checks on extracted raw data:
    - Completeness checks for critical columns
    - Amount validation
    """,
)

# Task 3: Transform data
transform_task = PythonOperator(
    task_id="transform_data",
    python_callable=transform_data_task,
    dag=dag,
    doc_md="""
    ## Transform Data Task
    
    Applies business logic and transformations:
    - Status field standardization
    - Feature engineering for fraud detection
    - Data anonymization if required
    """,
)

# Task 4: Validate transformed data
validate_transformed = PythonOperator(
    task_id="validate_transformed_data",
    python_callable=validate_transformed_task,
    dag=dag,
    doc_md="""
    ## Validate Transformed Data Task
    
    Performs business rule validations:
    - Account ID consistency checks
    - Status enum validation
    """,
)

# Task 5: Load to Spark warehouse
load_task = PythonOperator(
    task_id="load_to_spark_warehouse",
    python_callable=load_to_spark_task,
    dag=dag,
    doc_md="""
    ## Load to Spark Warehouse Task
    
    Loads cleaned and transformed data into Apache Spark data warehouse
    using idempotent operations.
    """,
)

# Task 6: Train AI model (Bonus)
train_model_task = PythonOperator(
    task_id="train_ai_model",
    python_callable=train_ai_model_task,
    dag=dag,
    doc_md="""
    ## Train AI Model Task
    
    Initiates automated ML training using H2O.ai AutoML to train
    fraud detection model on updated dataset.
    """,
)

# Define task dependencies
(
    extract_task
    >> validate_extraction
    >> transform_task
    >> validate_transformed
    >> load_task
    >> train_model_task
)
