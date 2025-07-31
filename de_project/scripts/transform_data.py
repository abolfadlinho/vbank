import pandas as pd
import numpy as np
import logging
from datetime import datetime, timedelta
from airflow.exceptions import AirflowException

logger = logging.getLogger(__name__)

def transform_data_task(**context):
    """
    Apply business logic and transformations to raw transaction data.
    """
    try:
        # Get input path from extraction task
        ti = context['task_instance']
        input_path = ti.xcom_pull(task_ids='extract_transactions', key='output_path')
        
        if not input_path:
            raise AirflowException("No input path received from extraction task")
        
        # Load extracted data
        df = pd.read_parquet(input_path)
        logger.info(f"Transforming {len(df)} records")
        
        # Transformation 1: Standardize status field values
        df['status'] = df['status'].str.upper().str.strip()
        
        # Transformation 2: Create fraud detection features
        df = create_fraud_features(df)
        
        # Transformation 3: Anonymize sensitive data (optional)
        df = anonymize_sensitive_data(df)
        
        # Transformation 4: Add metadata columns
        df['processed_timestamp'] = datetime.now()
        df['batch_id'] = context['ds']  # Airflow execution date
        
        # Save transformed data
        output_path = '/tmp/transformed_transactions.parquet'
        df.to_parquet(output_path, index=False)
        
        logger.info(f"Transformation completed. Output saved to: {output_path}")
        logger.info(f"Added {len(df.columns) - 7} new feature columns")  # Original has 7 columns
        
        # Store metadata for downstream tasks
        ti.xcom_push(key='transformed_records', value=len(df))
        ti.xcom_push(key='output_path', value=output_path)
        ti.xcom_push(key='feature_columns', value=list(df.columns))
        
        return output_path
        
    except Exception as e:
        logger.error(f"Error in transformation task: {str(e)}")
        raise AirflowException(f"Data transformation failed: {str(e)}")

def create_fraud_features(df):
    """
    Create features relevant for fraud detection.
    """
    logger.info("Creating fraud detection features...")
    
    # Feature 1: Transaction amount categories
    df['amount_category'] = pd.cut(df['amount'], 
                                 bins=[0, 100, 1000, 10000, float('inf')],
                                 labels=['small', 'medium', 'large', 'very_large'])
    
    # Feature 2: Hour of day (potential fraud patterns)
    df['hour_of_day'] = pd.to_datetime(df['timestamp']).dt.hour
    df['is_night_transaction'] = ((df['hour_of_day'] >= 22) | (df['hour_of_day'] <= 6)).astype(int)
    
    # Feature 3: Weekend transaction flag
    df['is_weekend'] = pd.to_datetime(df['timestamp']).dt.dayofweek.isin([5, 6]).astype(int)
    
    # Feature 4: Round amount flag (suspicious pattern)
    df['is_round_amount'] = (df['amount'] % 100 == 0).astype(int)
    
    # Feature 5: Same account flag (should be caught in validation, but adding as feature)
    df['same_account_flag'] = (df['from_account_id'] == df['to_account_id']).astype(int)
    
    # Feature 6: Amount log transformation (for ML models)
    df['log_amount'] = np.log1p(df['amount'])
    
    # Feature 7: Transaction velocity features (would need historical data in production)
    # For now, creating placeholder features
    df['account_transaction_count'] = 1  # Placeholder - would calculate from historical data
    df['daily_transaction_amount'] = df['amount']  # Placeholder - would sum daily amounts
    
    logger.info("✓ Fraud detection features created successfully")
    return df

def anonymize_sensitive_data(df):
    """
    Mask or anonymize sensitive data if required.
    """
    logger.info("Applying data anonymization...")
    
    # Hash account IDs for privacy (keeping as string for now, would use proper hashing in production)
    df['from_account_id_hash'] = df['from_account_id'].astype(str).str[-8:]  # Last 8 chars as simple anonymization
    df['to_account_id_hash'] = df['to_account_id'].astype(str).str[-8:]
    
    # Truncate or anonymize descriptions if they contain sensitive info
    if 'description' in df.columns:
        df['description_length'] = df['description'].str.len().fillna(0)
        df['has_description'] = df['description'].notna().astype(int)
        # In production, you might remove or further anonymize the description field
    
    logger.info("✓ Data anonymization completed")
    return df
