import pandas as pd
import logging
from airflow.exceptions import AirflowException

logger = logging.getLogger(__name__)

def validate_extraction_task(**context):
    """
    Perform initial data quality checks on extracted raw data.
    """
    try:
        # Get the output path from previous task
        ti = context['task_instance']
        input_path = ti.xcom_pull(task_ids='extract_transactions', key='output_path')
        
        if not input_path:
            raise AirflowException("No input path received from extraction task")
        
        # Load extracted data
        df = pd.read_parquet(input_path)
        logger.info(f"Validating {len(df)} extracted records")
        
        # Check 1: Completeness - Check for NULL values in critical columns
        critical_columns = ['id', 'from_account_id', 'to_account_id', 'amount', 'status']
        
        for column in critical_columns:
            null_count = df[column].isnull().sum()
            if null_count > 0:
                raise AirflowException(f"Found {null_count} NULL values in critical column: {column}")
        
        logger.info("✓ Completeness check passed - No NULL values in critical columns")
        
        # Check 2: Consistency - Validate that amount is positive numerical value
        if not pd.api.types.is_numeric_dtype(df['amount']):
            raise AirflowException("Amount column is not numeric")
        
        negative_amounts = df[df['amount'] <= 0]
        if len(negative_amounts) > 0:
            raise AirflowException(f"Found {len(negative_amounts)} records with non-positive amounts")
        
        logger.info("✓ Consistency check passed - All amounts are positive numerical values")
        
        # Additional checks
        duplicate_ids = df[df.duplicated(subset=['id'])]
        if len(duplicate_ids) > 0:
            raise AirflowException(f"Found {len(duplicate_ids)} duplicate transaction IDs")
        
        logger.info("✓ Uniqueness check passed - No duplicate transaction IDs")
        
        # Store validation results
        validation_results = {
            'total_records': len(df),
            'critical_columns_complete': True,
            'amounts_valid': True,
            'no_duplicates': True
        }
        
        ti.xcom_push(key='validation_results', value=validation_results)
        logger.info("All extraction data validation checks passed successfully")
        
        return True
        
    except Exception as e:
        logger.error(f"Validation failed: {str(e)}")
        raise AirflowException(f"Data validation failed: {str(e)}")
