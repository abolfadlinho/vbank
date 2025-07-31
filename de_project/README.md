# Fraud Detection ETL Pipeline

A comprehensive data engineering pipeline for fraud detection using Apache Airflow, Apache Spark, and H2O.ai AutoML.

## 🏗️ Architecture Overview

This pipeline implements a complete ETL (Extract, Transform, Load) workflow that:

1. **Extracts** transaction data from PostgreSQL database incrementally
2. **Validates** data quality at multiple stages
3. **Transforms** raw data with fraud-specific feature engineering
4. **Loads** processed data into Apache Spark data warehouse
5. **Trains** machine learning models using H2O.ai AutoML

## 📁 Project Structure

```
fraud_detection_pipeline/
├── dags/
│   └── fraud_etl_pipeline.py          # Main Airflow DAG
├── scripts/
│   ├── extract_transactions.py        # Data extraction logic
│   ├── validate_extraction_data.py    # Initial data validation
│   ├── transform_data.py              # Data transformation & feature engineering
│   ├── validate_transformed_data.py   # Business rule validation
│   ├── load_to_spark_warehouse.py     # Spark warehouse loading
│   └── train_ai_model.py              # H2O.ai model training
├── configs/
│   ├── db_config.json                 # Database configuration
│   └── h2o_config.json               # H2O.ai configuration
├── docker/
│   ├── docker-compose.yml            # Multi-service Docker setup
│   ├── Dockerfile                    # Custom Airflow image
│   └── init.sql                      # PostgreSQL initialization
├── requirements.txt                   # Python dependencies
└── README.md                         # This file
```

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- At least 8GB RAM available
- Ports 5432, 7077, 8080, 8081, 54321 available

### 1. Clone and Setup

```bash
git clone <repository-url>
cd fraud_detection_pipeline
```

### 2. Configure Environment

Edit `configs/db_config.json` with your database credentials:

```json
{
  "postgresql": {
    "host": "postgres",
    "port": "5432",
    "database": "fraud_detection",
    "user": "postgres",
    "password": "your_secure_password"
  }
}
```

### 3. Start Services

```bash
cd docker
docker-compose up -d
```

This will start:

- **PostgreSQL** (port 5432) - Source database
- **Airflow Webserver** (port 8080) - Pipeline orchestration UI
- **Airflow Scheduler** - Task scheduling service
- **Spark Master** (port 8081) - Distributed computing
- **Spark Worker** - Computation node
- **H2O.ai Server** (port 54321) - AutoML platform

### 4. Access Services

- **Airflow UI**: http://localhost:8080 (admin/admin)
- **Spark UI**: http://localhost:8081
- **H2O Flow**: http://localhost:54321

### 5. Run the Pipeline

1. Open Airflow UI
2. Enable the `fraud_detection_etl_pipeline` DAG
3. Trigger a manual run or wait for scheduled execution

## 📊 Pipeline Tasks

### Task 1: Extract Transactions

- Connects to PostgreSQL database
- Performs incremental extraction using timestamps
- Handles last extraction timestamp tracking
- Outputs: Raw transaction data in Parquet format

### Task 2: Validate Extraction Data

- **Completeness checks**: NULL values in critical columns
- **Consistency checks**: Positive amount validation
- **Uniqueness checks**: Duplicate transaction ID detection
- Fails pipeline if validation errors found

### Task 3: Transform Data

- **Status standardization**: Normalize enum values
- **Feature engineering**: Creates fraud-specific features:
  - Amount categories (small/medium/large/very_large)
  - Time-based features (hour, night transactions, weekends)
  - Behavioral patterns (round amounts, transaction velocity)
  - Log transformations for ML compatibility
- **Data anonymization**: Hash account IDs, sanitize descriptions

### Task 4: Validate Transformed Data

- **Business rule validation**:
  - Account ID consistency (from ≠ to)
  - Valid status enum values only
- **Feature validation**: Ensure all engineered features created
- **Data integrity**: Check for corruption during transformation

### Task 5: Load to Spark Warehouse

- Loads processed data into partitioned Spark tables
- **Idempotent operations**: Partition overwrite strategy
- **Partitioning**: By date for efficient querying
- Creates `fraud_detection.transactions` table

### Task 6: Train AI Model (Bonus)

- Uses H2O.ai AutoML for automated model training
- **Feature selection**: Fraud-relevant features
- **Model evaluation**: AUC, accuracy, precision, recall
- **Artifact management**: Saves models and performance metrics
- **Versioning**: Date-based model versioning

## 🔧 Configuration

### Database Configuration

Update `configs/db_config.json`:

```json
{
  "postgresql": {
    "host": "your-db-host",
    "port": "5432",
    "database": "fraud_detection",
    "user": "your-username",
    "password": "your-password"
  }
}
```

### H2O.ai Configuration

Update `configs/h2o_config.json`:

```json
{
  "ip": "h2o",
  "port": 54321,
  "max_mem_size": "4G"
}
```

### Airflow Variables

Set in Airflow UI under Admin > Variables:

- `last_extraction_timestamp`: Controls incremental loading

## 📈 Monitoring & Observability

### Airflow Monitoring

- **Task logs**: Available in Airflow UI
- **XCom variables**: Track data flow between tasks
- **Email alerts**: Configure SMTP for failure notifications
- **Metrics**: Task duration, success rates, data volumes

### Data Quality Metrics

Each validation task stores results in XCom:

```python
{
  "total_records": 1000,
  "validation_passed": true,
  "error_count": 0,
  "timestamp": "2024-01-15T10:30:00"
}
```

### Model Performance Tracking

Model training saves comprehensive metrics:

```json
{
  "model_id": "GLM_1_AutoML_20240115_103000",
  "auc": 0.9234,
  "accuracy": 0.8876,
  "precision": 0.7845,
  "recall": 0.8123,
  "training_date": "2024-01-15"
}
```

## 🔒 Security Considerations

### Data Privacy

- Account IDs are hashed during transformation
- Sensitive descriptions are anonymized
- PII data is not stored in logs

### Access Control

- Database credentials stored in configuration files
- Airflow connections for secure credential management
- Network isolation using Docker networks

### Audit Trail

- All data operations logged with timestamps
- Model training history maintained
- Data lineage tracked through XCom

## 🚨 Error Handling & Recovery

### Automatic Retries

- Tasks configured with 2 automatic retries
- 5-minute delay between retries
- Email notifications on final failure

### Data Validation Failures

- Pipeline stops immediately on validation errors
- Detailed error messages in task logs
- Manual intervention required for data quality issues

### Recovery Procedures

1. **Failed Extraction**: Check database connectivity and permissions
2. **Validation Errors**: Review data quality issues, fix source data
3. **Transformation Failures**: Check feature engineering logic
4. **Load Failures**: Verify Spark cluster status and storage
5. **Model Training Issues**: Check H2O.ai cluster health

## 📝 Development Guide

### Adding New Features

1. Update `transform_data.py` with new feature logic
2. Add validation rules in `validate_transformed_data.py`
3. Update model training feature list in `train_ai_model.py`
4. Test with sample data

### Custom Validations

```python
def custom_validation_check(df):
    """Add custom business rule validation."""
    # Your validation logic here
    if validation_failed:
        raise AirflowException("Custom validation failed")
    return True
```

### Extending the Pipeline

- Add new tasks to the DAG
- Update task dependencies
- Configure appropriate retries and alerts
- Document new functionality

## 🧪 Testing

### Local Testing

```bash
# Test individual scripts
python scripts/extract_transactions.py
python scripts/transform_data.py

# Validate DAG syntax
python dags/fraud_etl_pipeline.py
```

### Integration Testing

1. Start Docker services
2. Load test data into PostgreSQL
3. Trigger DAG manually
4. Verify data flow through all stages
5. Check model training completion

## 📋 Troubleshooting

### Common Issues

**Docker Services Won't Start**

```bash
# Check port conflicts
netstat -tlnp | grep 8080

# Check Docker resources
docker system df
docker system prune
```

**Airflow DAG Not Appearing**

- Check DAG syntax: `python dags/fraud_etl_pipeline.py`
- Verify file permissions
- Check Airflow logs: `docker-compose logs airflow-scheduler`

**Spark Connection Issues**

- Verify Spark master is running: http://localhost:8081
- Check Spark worker connectivity
- Review Spark application logs

**H2O.ai Training Failures**

- Check H2O cluster status: http://localhost:54321
- Verify memory allocation
- Review training data format

**Database Connection Errors**

- Verify PostgreSQL is running
- Check credentials in `db_config.json`
- Test connection manually

### Performance Optimization

**For Large Datasets**

- Increase Spark worker memory
- Adjust partition sizes
- Enable Spark adaptive query execution
- Consider data compression

**For Frequent Runs**

- Optimize incremental loading queries
- Implement data caching strategies
- Tune H2O.ai memory settings
- Use Airflow task pools

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/new-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/new-feature`
5. Submit pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For issues and questions:

1. Check the troubleshooting section
2. Review Airflow and Spark documentation
3. Open an issue on GitHub
4. Contact the data engineering team

---
