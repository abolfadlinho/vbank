
import logging
import h2o
from h2o.automl import H2OAutoML
from airflow.exceptions import AirflowException
import json
import os
from datetime import datetime

logger = logging.getLogger(__name__)

def train_ai_model_task(**context):
    """
    Train fraud detection model using H2O.ai AutoML.
    """
    try:
        # Get warehouse information from previous task
        ti = context['task_instance']
        warehouse_table = ti.xcom_pull(task_ids='load_to_spark_warehouse', key='warehouse_table')
        
        if not warehouse_table:
            raise AirflowException("No warehouse table information received")
        
        # Initialize H2O
        h2o_config = get_h2o_config()
        h2o.init(ip=h2o_config['ip'], port=h2o_config['port'])
        
        logger.info("H2O cluster initialized successfully")
        
        # Load data from Spark warehouse (in production, you'd connect to actual Spark)
        # For this example, we'll simulate loading the data
        training_data = load_training_data_from_warehouse(warehouse_table)
        
        # Prepare data for training
        training_frame = prepare_training_data(training_data)
        
        # Configure AutoML
        target_column = 'is_fraud'  # This would be your fraud label
        feature_columns = get_feature_columns()
        
        # Create and train AutoML model
        automl = H2OAutoML(
            max_models=10,
            max_runtime_secs=300,  # 5 minutes for demo
            seed=42,
            project_name=f"fraud_detection_{context['ds']}"
        )
        
        logger.info("Starting AutoML training...")
        automl.train(x=feature_columns, y=target_column, training_frame=training_frame)
        
        # Get best model
        best_model = automl.leader
        logger.info(f"Best model: {best_model.model_id}")
        
        # Save model and metrics
        model_path = save_model_artifacts(best_model, automl, context['ds'])
        
        # Store results for monitoring
        ti.xcom_push(key='model_id', value=best_model.model_id)
        ti.xcom_push(key='model_path', value=model_path)
        ti.xcom_push(key='training_completed', value=True)
        
        logger.info("Model training completed successfully")
        
        # Shutdown H2O
        h2o.cluster().shutdown()
        
        return model_path
        
    except Exception as e:
        logger.error(f"Error in AI model training: {str(e)}")
        raise AirflowException(f"AI model training failed: {str(e)}")

def get_h2o_config():
    """Get H2O.ai configuration."""
    config_path = '/opt/airflow/dags/configs/h2o_config.json'
    
    if os.path.exists(config_path):
        with open(config_path, 'r') as f:
            return json.load(f)
    else:
        return {
            'ip': os.getenv('H2O_IP', 'localhost'),
            'port': int(os.getenv('H2O_PORT', '54321'))
        }

def load_training_data_from_warehouse(warehouse_table):
    """
    Load training data from Spark warehouse.
    In production, this would connect to actual Spark cluster.
    """
    # Simulated data loading - in production, use Spark connector
    logger.info(f"Loading training data from {warehouse_table}")
    
    # This is a simulation - in reality you'd query Spark
    import pandas as pd
    import numpy as np
    
    # Create sample data for demonstration
    np.random.seed(42)
    n_samples = 1000
    
    data = {
        'amount': np.random.lognormal(5, 2, n_samples),
        'hour_of_day': np.random.randint(0, 24, n_samples),
        'is_night_transaction': np.random.binomial(1, 0.3, n_samples),
        'is_weekend': np.random.binomial(1, 0.2, n_samples),
        'is_round_amount': np.random.binomial(1, 0.1, n_samples),
        'log_amount': np.random.normal(5, 1, n_samples),
        'is_fraud': np.random.binomial(1, 0.05, n_samples)  # 5% fraud rate
    }
    
    return pd.DataFrame(data)

def prepare_training_data(df):
    """Prepare pandas DataFrame for H2O training."""
    # Convert to H2O Frame
    training_frame = h2o.H2OFrame(df)
    
    # Convert target to factor for classification
    training_frame['is_fraud'] = training_frame['is_fraud'].asfactor()
    
    logger.info(f"Training data prepared: {training_frame.nrows} rows, {training_frame.ncols} columns")
    return training_frame

def get_feature_columns():
    """Get list of feature columns for training."""
    return [
        'amount', 'hour_of_day', 'is_night_transaction', 
        'is_weekend', 'is_round_amount', 'log_amount'
    ]

def save_model_artifacts(best_model, automl, batch_date):
    """Save model artifacts and performance metrics."""
    # Create model directory
    model_dir = f"/opt/airflow/models/fraud_detection_{batch_date}"
    os.makedirs(model_dir, exist_ok=True)
    
    # Save model
    model_path = h2o.save_model(best_model, path=model_dir, force=True)
    logger.info(f"Model saved to: {model_path}")
    
    # Save performance metrics
    performance = best_model.model_performance()
    metrics = {
        'model_id': best_model.model_id,
        'auc': float(performance.auc()[0][0]),
        'accuracy': float(performance.accuracy()[0][0]),
        'precision': float(performance.precision()[0][0]),
        'recall': float(performance.recall()[0][0]),
        'training_date': batch_date,
        'timestamp': datetime.now().isoformat()
    }
    
    # Save metrics to file
    metrics_path = os.path.join(model_dir, 'metrics.json')
    with open(metrics_path, 'w') as f:
        json.dump(metrics, f, indent=2)
    
    logger.info(f"Model metrics saved to: {metrics_path}")
    logger.info(f"Model AUC: {metrics['auc']:.4f}")
    
    return model_path
