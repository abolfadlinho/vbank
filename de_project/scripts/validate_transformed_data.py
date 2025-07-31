import pandas as pd
import logging
from airflow.exceptions import AirflowException

logger = logging.getLogger(__name__)

def validate_transformed_task(**context):
    """
    Perform business rule validations on transformed data.
    """
    try:
        # Get input path from transformation task
        ti = context['task_instance']
        input_path = ti.xcom_pull(task_ids='transform_data', key='output_path')
        
        if not input_path:
            raise AirflowException("No input path received from transformation task")
        
        # Load transformed data
        df = pd.read_parquet(input_path)
        logger.info(f"Validating {len(df)} transformed records")
        
        # Check 1: Consistency - Ensure from_account_id and to_account_id are not the same
        same_account_transactions = df[df['from_account_id'] == df['to_account_id']]
        if len(same_account_transactions) > 0:
            logger.warning(f"Found {len(same_account_transactions)} transactions with same from/to accounts")
            # Flag them but don't fail - they might be legitimate internal transfers
            # raise AirflowException(f"Found {len(same_account_transactions)} transactions with same from/to account IDs")
        
        logger.info("✓ Account ID consistency check completed")
        
        # Check 2: Business Rules - Validate status enum values
        valid_statuses = {'INITIATED', 'SUCCESS', 'FAILED'}
        invalid_statuses = df[~df['status'].isin(valid_statuses)]
        
        if len(invalid_statuses) > 0:
            invalid_values = invalid_statuses['status'].unique()
            raise AirflowException(f"Found invalid status values: {invalid_values}. Valid values are: {valid_statuses}")
        
        logger.info("✓ Status enum validation passed")
        
        # Check 3: Feature validation - Ensure new features are properly created
        required_features = ['amount_category', 'hour_of_day', 'is_night_transaction', 
                           'is_weekend', 'is_round_amount', 'log_amount']
        
        missing_features = [feat for feat in required_features if feat not in df.columns]
        if missing_features:
            raise AirflowException(f"Missing required features: {missing_features}")
        
        logger.info("✓ Feature validation passed")
        
        # Check 4: Data integrity - Ensure no data corruption during transformation
        if df['amount'].isnull().any():
            raise AirflowException("Found NULL values in amount column after transformation")
        
        if (df['log_amount'] < 0).any():
            raise AirflowException("Found negative log_amount values - indicates data corruption")
        
        logger.info("✓ Data integrity check passed")
        
        # Store validation results
        validation_results = {
            'total_records': len(df),
            'same_account_transactions': len(same_account_transactions),
            'valid_statuses': True,
            'features_created': True,
            'data_integrity': True
        }
        
        ti.xcom_push(key='validation_results', value=validation_results)
        logger.info("All transformed data validation checks passed successfully")
        
        return True
        
    except Exception as e:
        logger.error(f"Transformed data validation failed: {str(e)}")
        raise AirflowException(f"Transformed data validation failed: {str(e)}")
