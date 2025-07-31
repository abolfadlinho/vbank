import psycopg2
import pandas as pd
import json
import os
from datetime import datetime
from airflow.models import Variable
import logging

logger = logging.getLogger(__name__)

def extract_transactions_task(**context):
    """
    Extract transactions from PostgreSQL database using incremental loading.
    """
    try:
        # Load database configuration
        config = load_db_config()
        
        # Get last extraction timestamp
        last_timestamp = get_last_extraction_timestamp()
        
        # Connect to PostgreSQL
        conn = psycopg2.connect(
            host=config['host'],
            port=config['port'],
            database=config['database'],
            user=config['user'],
            password=config['password']
        )
        
        # Build incremental query
        query = """
        SELECT id, from_account_id, to_account_id, amount, description, status, timestamp
        FROM transactions
        WHERE timestamp > %s
        ORDER BY timestamp;
        """
        
        logger.info(f"Extracting transactions since: {last_timestamp}")
        
        # Execute query and load into DataFrame
        df = pd.read_sql_query(query, conn, params=[last_timestamp])
        
        logger.info(f"Extracted {len(df)} new transactions")
        
        # Save extracted data to temporary location
        output_path = '/tmp/extracted_transactions.parquet'
        df.to_parquet(output_path, index=False)
        
        # Update last extraction timestamp
        if not df.empty:
            new_timestamp = df['timestamp'].max()
            update_last_extraction_timestamp(new_timestamp)
            logger.info(f"Updated last extraction timestamp to: {new_timestamp}")
        
        # Store metadata for downstream tasks
        context['task_instance'].xcom_push(key='extracted_records', value=len(df))
        context['task_instance'].xcom_push(key='output_path', value=output_path)
        
        conn.close()
        return output_path
        
    except Exception as e:
        logger.error(f"Error in extraction task: {str(e)}")
        raise

def load_db_config():
    """Load database configuration from file or environment variables."""
    config_path = '/opt/airflow/dags/configs/db_config.json'
    
    if os.path.exists(config_path):
        with open(config_path, 'r') as f:
            return json.load(f)['postgresql']
    else:
        # Fallback to environment variables
        return {
            'host': os.getenv('POSTGRES_HOST', 'localhost'),
            'port': os.getenv('POSTGRES_PORT', '5432'),
            'database': os.getenv('POSTGRES_DB', 'fraud_detection'),
            'user': os.getenv('POSTGRES_USER', 'postgres'),
            'password': os.getenv('POSTGRES_PASSWORD', 'password')
        }

def get_last_extraction_timestamp():
    """Get the last extraction timestamp from Airflow Variables."""
    try:
        timestamp_str = Variable.get('last_extraction_timestamp')
        return datetime.fromisoformat(timestamp_str)
    except:
        # Default to epoch if not set
        return datetime(1970, 1, 1)

def update_last_extraction_timestamp(timestamp):
    """Update the last extraction timestamp in Airflow Variables."""
    Variable.set('last_extraction_timestamp', timestamp.isoformat())
