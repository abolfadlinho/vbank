# 🚀 Virtual Banking System

A comprehensive, enterprise-grade virtual banking platform designed to control, monitor, and analyze bank transactions. This project demonstrates modern software architecture, robust security, advanced data engineering, and automated fraud detection—all built for scalability and innovation.

---

## 📦 Table of Contents

1. [Project Structure](#project-structure)
2. [Architecture Overview](#architecture-overview)
3. [System Components](#system-components)
4. [Security Features](#security-features)
5. [Getting Started](#getting-started)
6. [Data Flow](#data-flow)
7. [Monitoring & Analytics](#monitoring--analytics)
8. [Fraud Detection Pipeline](#fraud-detection)
9. [Contributing](#contributing)
10. [License](#license)

---

## 📁 Project Structure

The repository is organized into modular microservices and a data engineering pipeline:

```
vbank/
├── accountservice/         # Account management microservice (Java Spring Boot)
│   ├── pom.xml             # Maven build configuration
│   └── src/main/java/      # Java source code
│   └── src/main/resources/ # Service configuration files
│   └── src/test/java/      # Unit and integration tests
├── bffservice/             # Backend-for-Frontend API gateway
│   ├── pom.xml
│   └── src/main/java/
├── loggingservice/         # Logging and audit microservice
│   ├── pom.xml
│   └── src/main/java/
├── transactionservice/     # Transaction processing microservice
│   ├── pom.xml
│   └── src/main/java/
├── userservice/            # User management microservice
│   ├── pom.xml
│   ├── data/               # H2 database files for local dev
│   └── src/main/java/
├── de_project/             # Data engineering and ML pipeline
│   ├── dags/               # Airflow DAGs for ETL workflows
│   ├── scripts/            # Python scripts for data extraction, transformation, validation, and ML
│   ├── configs/            # JSON configs for DB and H2O.ai
│   ├── docker/             # Dockerfile and docker-compose for orchestration
│   └── requirements.txt    # Python dependencies
├── Project Description (without WSO2).pdf # System overview document
├── README.md               # Project documentation
└── Running Notes.txt       # Developer notes and instructions
```

### Notable Nested Files

- `accountservice/pom.xml`: Maven configuration for building the account service.
- `accountservice/src/main/resources/application.properties`: Service-specific settings (DB, Kafka, etc).
- `de_project/dags/fraud_etl_pipeline.py`: Airflow DAG orchestrating ETL and fraud detection.
- `de_project/scripts/train_ai_model.py`: Python script for H2O.ai model training.
- `de_project/docker/docker-compose.yml`: Multi-container orchestration for data engineering.
- `userservice/data/user_service_db.mv.db`: H2 database file for user service (local dev/testing).

---

## 🏗️ Architecture Overview

The system is built using a microservices architecture, divided into two main modules:

### 1. Backend Module

- **Technology Stack**: Java Spring Boot
- **Architecture**: Microservices with API Gateway
- **Security**: WSO2 API Manager
- **Message Streaming**: Apache Kafka
- **Database**: PostgreSQL

### 2. Data Engineering Module

- **Orchestration**: Apache Airflow
- **Data Warehouse**: Apache Spark
- **ML Platform**: H2O.ai AutoML
- **Containerization**: Docker

---

## 🔧 System Components

### Backend Microservices

#### Core Services

- **User Service**: Authentication, authorization, profile management
- **Account Service**: Account creation, management, balance operations
- **Transaction Service**: Real-time transaction processing and validation

#### Supporting Services

- **BFF (Backend for Frontend)**: API aggregation layer for frontend clients
- **Logging Service**: Packet logging for performance and audit trails

### Data Pipeline

#### ETL Workflow (Apache Airflow)

1. **Extract**: Periodic data extraction from PostgreSQL
2. **Transform**: Python-based data cleansing and transformation
3. **Load**: Storage in Apache Spark data warehouse

#### Data Quality Assurance

- **Completeness Validation**: Ensures all required fields are present
- **Consistency Checks**: Validates data integrity across services
- **Business Rule Compliance**: Enforces banking domain constraints

#### AI/ML Integration

- **Automated Model Training**: H2O.ai AutoML pipeline for fraud detection
- **Real-time Inference**: Models deployed to transaction service
- **Continuous Learning**: Retraining with new transaction data

---

## 🔐 Security Features

### API Security (WSO2)

- Centralized API gateway for endpoint access control
- Authentication and authorization management
- Rate limiting and throttling
- API versioning and lifecycle management

### Data Security

- End-to-end encryption for sensitive data
- Secure database connections
- Audit logging for compliance
- Role-based access control (RBAC)

---

## 🚀 Getting Started

### Prerequisites

- Java 11+
- Docker & Docker Compose
- PostgreSQL
- Apache Kafka
- Apache Airflow
- Apache Spark

### Clone the Repository

```bash
git clone https://github.com/abolfadlinho/vbank.git
cd vbank
```

### Configuration

#### Database Setup

```sql
-- Create required databases
CREATE DATABASE users_db;
CREATE DATABASE accounts_db;
CREATE DATABASE transactions_db;
CREATE DATABASE logs_db;
```

#### Kafka Topics

```bash
# Create necessary Kafka topics
kafka-topics.sh --create --topic vbank-logs --bootstrap-server localhost:9092
```

### Running Microservices

Navigate to the desired service folder and run:

```powershell
.\mvnw.cmd spring-boot:run
```

### Running Data Engineering Project

Navigate to `de_project/` and follow its README. Typical steps:

```powershell
python scripts/extract_transactions.py
docker-compose up
```

---

## 📊 Data Flow

```
[Frontend] ↔ [BFF Service] ↔ [Core Services] → [Kafka] → [Data Pipeline]
                ↓                              ↓
          [WSO2 Gateway]                [Spark Warehouse]
                ↓                              ↓
          [Logging Service]              [H2O.ai ML Models]
```

---

## 🔍 Monitoring & Analytics

### Performance Monitoring

- Real-time transaction monitoring
- Service health checks
- Database performance metrics
- API response time tracking

### Business Intelligence

- Transaction volume analytics
- User behavior analysis
- Fraud detection metrics
- Compliance reporting

---

## 🤖 Fraud Detection Pipeline

The system implements an automated fraud detection pipeline:

1. **Data Collection**: Transaction patterns and user behavior
2. **Feature Engineering**: Automated feature extraction using H2O.ai
3. **Model Training**: AutoML-based model selection and training
4. **Real-time Scoring**: Integration with transaction service
5. **Continuous Improvement**: Model retraining with new data

---

## 🤝 Contributing

We welcome contributions! Please fork the repository, create a feature branch, and submit a pull request. For major changes, open an issue first to discuss what you would like to change.

---

## 📄 License

This project is licensed under the MIT License.
