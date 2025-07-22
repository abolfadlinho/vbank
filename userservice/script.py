import jpype
import jpype.imports
from jpype.types import *
import os
import uuid
import random
from datetime import datetime, timedelta
from faker import Faker

# --- Configuration ---
# IMPORTANT: Replace with the actual path to your H2 JAR file.
# This JAR is typically found in the 'bin' directory of your H2 installation.
H2_JAR_PATH = "C:/Program Files (x86)/H2/bin/h2-2.3.232.jar" # e.g., "C:/H2/bin/h2-2.2.222.jar"

# Define the base directory for your H2 database files
# This should match the './data/' prefix used in your Spring Boot application.properties
DB_BASE_DIR = "./data"

# Ensure the database directory exists
os.makedirs(DB_BASE_DIR, exist_ok=True)

# Database connection URLs (file-based, matching Spring Boot config)
DB_URLS = {
    "user_service_db": f"jdbc:h2:file:{DB_BASE_DIR}/user_service_db;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
    "account_service_db": f"jdbc:h2:file:{DB_BASE_DIR}/account_service_db;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
    "transaction_service_db": f"jdbc:h2:file:{DB_BASE_DIR}/transaction_service_db;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
    "logging_service_db": f"jdbc:h2:file:{DB_BASE_DIR}/logging_service_db;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
}

DB_USER = "sa"
DB_PASSWORD = "" # Default H2 password is empty

# Number of records to generate for each table
NUM_USERS = 50
NUM_ACCOUNTS_PER_USER = 2 # Each user will have this many accounts
NUM_TRANSACTIONS = 200
NUM_LOGS = 500

# Initialize Faker for realistic data generation
fake = Faker()

# --- H2 Database Schema SQL (matching your PDF, with H2 compatible UUID generation) ---
# Note: H2's ENUM type is a bit specific. If you encounter issues, consider using VARCHAR
# and enforcing the enum values in your application logic.
SQL_SCHEMAS = {
    "user_service_db": """
        CREATE TABLE IF NOT EXISTS users (
            id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            password_hash VARCHAR(255) NOT NULL,
            first_name VARCHAR(50) NOT NULL,
            last_name VARCHAR(50) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """,
    "account_service_db": """
        CREATE TABLE IF NOT EXISTS accounts (
            id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
            user_id UUID,
            account_number VARCHAR(20) UNIQUE NOT NULL,
            account_type ENUM('SAVINGS', 'CHECKING') NOT NULL,
            balance DECIMAL(15,2) DEFAULT 0.00,
            status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """,
    "transaction_service_db": """
        CREATE TABLE IF NOT EXISTS transactions (
            id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
            from_account_id UUID NOT NULL,
            to_account_id UUID NOT NULL,
            amount DECIMAL(15,2) NOT NULL,
            description VARCHAR(255),
            status ENUM('INITIATED', 'SUCCESS', 'FAILED') DEFAULT 'INITIATED',
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
    """,
    "logging_service_db": """
        CREATE TABLE IF NOT EXISTS logs (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
            message TEXT NOT NULL,
            message_type ENUM('Request', 'Response') NOT NULL,
            date_time TIMESTAMP NOT NULL
        );
    """
}

# --- JPype and JDBC Setup ---
def start_jvm():
    """Starts the JVM and adds the H2 JAR to the classpath."""
    if not jpype.isJVMStarted():
        try:
            jpype.startJVM(jpype.getDefaultJVMPath(), f"-Djava.class.path={H2_JAR_PATH}")
            print(f"JVM started successfully with H2 JAR: {H2_JAR_PATH}")
        except Exception as e:
            print(f"Error starting JVM: {e}")
            print("Please ensure H2_JAR_PATH is correct and Java is installed.")
            exit(1)

def get_connection(db_url):
    """Establishes a JDBC connection to the H2 database."""
    try:
        java_sql = jpype.JPackage("java.sql")
        conn = java_sql.DriverManager.getConnection(db_url, DB_USER, DB_PASSWORD)
        print(f"Connected to {db_url}")
        return conn
    except Exception as e:
        print(f"Error connecting to database {db_url}: {e}")
        return None

def execute_sql(conn, sql_statement):
    """Executes a single SQL statement."""
    try:
        stmt = conn.createStatement()
        stmt.execute(sql_statement)
        stmt.close()
        print(f"Executed SQL: {sql_statement.splitlines()[0]}...")
    except Exception as e:
        print(f"Error executing SQL: {sql_statement.splitlines()[0]}...\nError: {e}")

def create_tables():
    """Creates all necessary tables in their respective databases."""
    for db_name, sql in SQL_SCHEMAS.items():
        print(f"\n--- Creating tables for {db_name} ---")
        conn = get_connection(DB_URLS[db_name])
        if conn:
            execute_sql(conn, sql)
            conn.close()

# --- Data Generation Functions ---

def populate_users(conn):
    """Populates the users table with random data."""
    print(f"\n--- Populating users table in {conn.getMetaData().getURL()} ---")
    users_data = []
    user_ids = []
    try:
        pstmt = conn.prepareStatement(
            "INSERT INTO users (id, username, email, password_hash, first_name, last_name, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )
        for _ in range(NUM_USERS):
            user_id = uuid.uuid4()
            username = fake.user_name() + str(random.randint(100, 999)) # Ensure uniqueness
            email = fake.email()
            password_hash = fake.sha256() # Placeholder for hashed password
            first_name = fake.first_name()
            last_name = fake.last_name()
            created_at = fake.date_time_between(start_date="-2y", end_date="now")

            pstmt.setString(1, str(user_id))
            pstmt.setString(2, username)
            pstmt.setString(3, email)
            pstmt.setString(4, password_hash)
            pstmt.setString(5, first_name)
            pstmt.setString(6, last_name)
            pstmt.setTimestamp(7, jpype.JClass("java.sql.Timestamp").valueOf(created_at.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]))
            pstmt.addBatch()
            user_ids.append(user_id)
        pstmt.executeBatch()
        conn.commit()
        pstmt.close()
        print(f"Inserted {NUM_USERS} users.")
    except Exception as e:
        print(f"Error populating users: {e}")
        conn.rollback()
    return user_ids

def populate_accounts(conn, user_ids):
    """Populates the accounts table with random data."""
    print(f"\n--- Populating accounts table in {conn.getMetaData().getURL()} ---")
    account_ids = []
    if not user_ids:
        print("No users found to link accounts to. Skipping account population.")
        return []

    try:
        pstmt = conn.prepareStatement(
            "INSERT INTO accounts (id, user_id, account_number, account_type, balance, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )
        for user_id in user_ids:
            for _ in range(NUM_ACCOUNTS_PER_USER):
                account_id = uuid.uuid4()
                account_number = fake.unique.bothify(text='############') # 12 digit number
                account_type = random.choice(['SAVINGS', 'CHECKING'])
                balance = round(random.uniform(0.00, 100000.00), 2)
                status = random.choice(['ACTIVE', 'INACTIVE'])
                created_at = fake.date_time_between(start_date="-1y", end_date="now")
                updated_at = created_at + timedelta(days=random.randint(0, 365))

                pstmt.setString(1, str(account_id))
                pstmt.setString(2, str(user_id))
                pstmt.setString(3, account_number)
                pstmt.setString(4, account_type)
                pstmt.setDouble(5, balance)
                pstmt.setString(6, status)
                pstmt.setTimestamp(7, jpype.JClass("java.sql.Timestamp").valueOf(created_at.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]))
                pstmt.setTimestamp(8, jpype.JClass("java.sql.Timestamp").valueOf(updated_at.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]))
                pstmt.addBatch()
                account_ids.append(account_id)
        pstmt.executeBatch()
        conn.commit()
        pstmt.close()
        print(f"Inserted {len(account_ids)} accounts.")
    except Exception as e:
        print(f"Error populating accounts: {e}")
        conn.rollback()
    return account_ids

def populate_transactions(conn, account_ids):
    """Populates the transactions table with random data."""
    print(f"\n--- Populating transactions table in {conn.getMetaData().getURL()} ---")
    if len(account_ids) < 2:
        print("Not enough accounts to create transactions. Skipping transaction population.")
        return

    try:
        pstmt = conn.prepareStatement(
            "INSERT INTO transactions (id, from_account_id, to_account_id, amount, description, status, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)"
        )
        for _ in range(NUM_TRANSACTIONS):
            transaction_id = uuid.uuid4()
            from_account_id = random.choice(account_ids)
            # Ensure to_account_id is different from from_account_id
            to_account_id = random.choice([acc for acc in account_ids if acc != from_account_id])
            if from_account_id == to_account_id and len(account_ids) > 1: # Fallback if only one account left after filtering
                while to_account_id == from_account_id:
                    to_account_id = random.choice(account_ids)

            amount = round(random.uniform(1.00, 5000.00), 2)
            description = fake.sentence(nb_words=6)
            status = random.choice(['INITIATED', 'SUCCESS', 'FAILED'])
            timestamp = fake.date_time_between(start_date="-6m", end_date="now")

            pstmt.setString(1, str(transaction_id))
            pstmt.setString(2, str(from_account_id))
            pstmt.setString(3, str(to_account_id))
            pstmt.setDouble(4, amount)
            pstmt.setString(5, description)
            pstmt.setString(6, status)
            pstmt.setTimestamp(7, jpype.JClass("java.sql.Timestamp").valueOf(timestamp.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]))
            pstmt.addBatch()
        pstmt.executeBatch()
        conn.commit()
        pstmt.close()
        print(f"Inserted {NUM_TRANSACTIONS} transactions.")
    except Exception as e:
        print(f"Error populating transactions: {e}")
        conn.rollback()

def populate_logs(conn):
    """Populates the logs table with random data."""
    print(f"\n--- Populating logs table in {conn.getMetaData().getURL()} ---")
    try:
        pstmt = conn.prepareStatement(
            "INSERT INTO logs (message, message_type, date_time) VALUES (?, ?, ?)"
        )
        for _ in range(NUM_LOGS):
            message = fake.paragraph(nb_sentences=2)
            message_type = random.choice(['Request', 'Response'])
            date_time = fake.date_time_between(start_date="-1m", end_date="now")

            pstmt.setString(1, message)
            pstmt.setString(2, message_type)
            pstmt.setTimestamp(3, jpype.JClass("java.sql.Timestamp").valueOf(date_time.strftime("%Y-%m-%d %H:%M:%S.%f")[:-3]))
            pstmt.addBatch()
        pstmt.executeBatch()
        conn.commit()
        pstmt.close()
        print(f"Inserted {NUM_LOGS} logs.")
    except Exception as e:
        print(f"Error populating logs: {e}")
        conn.rollback()

# --- Main Execution ---
if __name__ == "__main__":
    print("Starting H2 Database Populator Script...")

    # 1. Start JVM
    start_jvm()

    if not jpype.isJVMStarted():
        print("JVM failed to start. Exiting.")
        exit(1)

    # Import Java classes after JVM starts
    from java.sql import DriverManager

    # 2. Create Tables
    create_tables()

    # 3. Populate Data
    all_user_ids = []
    all_account_ids = []

    # Populate User Service DB
    user_conn = get_connection(DB_URLS["user_service_db"])
    if user_conn:
        all_user_ids = populate_users(user_conn)
        user_conn.close()

    # Populate Account Service DB
    account_conn = get_connection(DB_URLS["account_service_db"])
    if account_conn:
        all_account_ids = populate_accounts(account_conn, all_user_ids)
        account_conn.close()

    # Populate Transaction Service DB
    transaction_conn = get_connection(DB_URLS["transaction_service_db"])
    if transaction_conn:
        populate_transactions(transaction_conn, all_account_ids)
        transaction_conn.close()

    # Populate Logging Service DB
    logging_conn = get_connection(DB_URLS["logging_service_db"])
    if logging_conn:
        populate_logs(logging_conn)
        logging_conn.close()

    # 4. Shutdown JVM
    jpype.shutdownJVM()
    print("\nH2 Database Populator Script Finished.")
