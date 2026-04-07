from airflow import DAG
from datetime import datetime
import os
from pathlib import Path

from airflow.providers.standard.operators.empty import EmptyOperator
from airflow.providers.standard.operators.bash import BashOperator
from airflow.providers.standard.operators.python import PythonOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from airflow.sdk import TaskGroup
from sqlalchemy import create_engine, text
from sqlalchemy.engine import URL
from scripts.check_hdfs_data import check_hdfs_data
from scripts.ingest_to_hdfs import ingest_to_hdfs
from scripts.prepare_silver_hdfs_paths import prepare_silver_hdfs_paths

SPARK_CONN_ID = "spark_default"
SPARK_APP_JAR = os.getenv(
    "SPARK_APP_JAR",
    "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
)
WAREHOUSE_HOST = os.getenv("WAREHOUSE_HOST", "postgres-warehouse")
WAREHOUSE_PORT = int(os.getenv("WAREHOUSE_PORT", "5432"))
WAREHOUSE_DB = os.getenv("WAREHOUSE_DB", "warehouse")
WAREHOUSE_USER = os.getenv("JDBC_USER", "warehouse_user")
WAREHOUSE_PASSWORD = os.getenv("JDBC_PASSWORD", "warehouse_pass")
SQL_SCHEMAS_DIR = Path(os.getenv("AIRFLOW_SQL_SCHEMAS_DIR", "/opt/airflow/sql/schemas"))
GOLD_SCHEMA_SQL = SQL_SCHEMAS_DIR / "create_gold_schema.sql"
GOLD_VIEWS_SQL = SQL_SCHEMAS_DIR / "create_gold_views.sql"


def get_warehouse_engine():
    return create_engine(
        URL.create(
            "postgresql+psycopg2",
            username=WAREHOUSE_USER,
            password=WAREHOUSE_PASSWORD,
            host=WAREHOUSE_HOST,
            port=WAREHOUSE_PORT,
            database=WAREHOUSE_DB,
        )
    )

def load_sql_text(sql_file_path, script_name):
    sql_file = Path(sql_file_path)
    if sql_file.exists():
        print(f"Loading {script_name} from {sql_file}")
        return sql_file.read_text(encoding="utf-8")

    raise FileNotFoundError(
        f"{sql_file} not found. Ensure ./sql is mounted into the Airflow containers "
        f"at {SQL_SCHEMAS_DIR} and recreate the Airflow services after docker-compose changes."
    )


def execute_sql_text(sql_text):
    statements = [statement.strip() for statement in sql_text.split(";") if statement.strip()]

    engine = get_warehouse_engine()
    with engine.begin() as connection:
        for statement in statements:
            connection.exec_driver_sql(statement)
    engine.dispose()

def create_gold_schema():
    execute_sql_text(load_sql_text(GOLD_SCHEMA_SQL, "gold schema"))

def create_power_bi_views():
    execute_sql_text(load_sql_text(GOLD_VIEWS_SQL, "Power BI views"))

def validate_gold_tables():
    engine = get_warehouse_engine()
    query = text(
        """
        SELECT 'gold.dim_customer' AS table_name, COUNT(*) AS row_count FROM gold.dim_customer
        UNION ALL
        SELECT 'gold.dim_product' AS table_name, COUNT(*) AS row_count FROM gold.dim_product
        UNION ALL
        SELECT 'gold.fact_sales' AS table_name, COUNT(*) AS row_count FROM gold.fact_sales
        """
    )
    with engine.connect() as connection:
        for row in connection.execute(query):
            print(f"{row.table_name}: {row.row_count}")
    engine.dispose()

with DAG(
    dag_id = "etl_dag",
    schedule = "0 0 * * *",
    start_date = datetime(2026,4,1),
    catchup = False
)as dag:
    start = EmptyOperator(task_id = "start")

    with TaskGroup("extract") as extract_group:
        ingest_crm_task = PythonOperator(
            task_id = "ingest_crm_to_hdfs",
            python_callable = ingest_to_hdfs,
            op_kwargs = {"source_path": "/opt/airflow/data/landing/crm","hdfs_path": "/data/bronze/crm"},
        )

        ingest_erp_task = PythonOperator(
            task_id = "ingest_erp_to_hdfs",
            python_callable = ingest_to_hdfs,
            op_kwargs = {"source_path": "/opt/airflow/data/landing/erp","hdfs_path": "/data/bronze/erp"},

        )

        check_crm_task = PythonOperator(
            task_id = "check_crm_data",
            python_callable = check_hdfs_data,
            op_kwargs = {"hdfs_path": "/data/bronze/crm","number_files":3},
        )

        check_erp_task = PythonOperator(
            task_id = "check_erp_data",
            python_callable = check_hdfs_data,
            op_kwargs = {"hdfs_path": "/data/bronze/erp","number_files":3},
        )
        ingest_crm_task >> check_crm_task >> ingest_erp_task >> check_erp_task
    with TaskGroup("transform") as transform_group:
        compile_jar = BashOperator(
            task_id = "compile_jar",
            bash_command = "cd /opt/airflow/spark && sbt clean assembly",
        )
        prepare_hdfs_output_paths = PythonOperator(
            task_id = "prepare_hdfs_output_paths",
            python_callable = prepare_silver_hdfs_paths,
        )
        transform_crm_demographics =SparkSubmitOperator(
            task_id = "transform_crm_demographics",
            conn_id = "spark_default",
            application = "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
            java_class = "com.crm_erp_analytics.silver.crm.CustomerDemographicsCleanJob",
        )   
        transform_crm_locations = SparkSubmitOperator(
            task_id = "transform_crm_locations",
            conn_id = "spark_default",
            application = "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
            java_class = "com.crm_erp_analytics.silver.crm.CustomerLocationsCleanJob",
        )
        transform_crm_master = SparkSubmitOperator(
            task_id = "transform_crm_master",
            conn_id = "spark_default",
            application = "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
            java_class = "com.crm_erp_analytics.silver.crm.CustomerMasterCleanJob",
        )

        transform_erp_categories = SparkSubmitOperator(
            task_id = "transform_erp_categories",
            conn_id = "spark_default",
            application = "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
            java_class = "com.crm_erp_analytics.silver.erp.ProductCategoriesCleanJob",
        )
        transform_erp_transactions = SparkSubmitOperator(
            task_id = "transform_erp_transactions",
            conn_id = "spark_default",
            application = "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
            java_class = "com.crm_erp_analytics.silver.erp.SalesTransactionsJob",
        )
        transform_erp_master = SparkSubmitOperator(
            task_id = "transform_erp_master",
            conn_id = "spark_default",
            application = "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
            java_class = "com.crm_erp_analytics.silver.erp.ProductMasterCleanJob",
        )
        prepare_hdfs_output_paths >> compile_jar 
        compile_jar >> transform_crm_demographics >> transform_crm_locations >> transform_crm_master
        compile_jar >> transform_erp_categories >> transform_erp_transactions >> transform_erp_master 
    with TaskGroup("load_dwh") as load_dwh_group:
        create_gold_schema_task = PythonOperator(
            task_id="create_gold_schema",
            python_callable=create_gold_schema,
        )
        dim_customer = SparkSubmitOperator(
            task_id = "dim_customer",
            conn_id = SPARK_CONN_ID,
            application = SPARK_APP_JAR,
            java_class = "com.crm_erp_analytics.gold.DimCustomerJob"
        )
        dim_product = SparkSubmitOperator(
            task_id = "dim_product",
            conn_id = SPARK_CONN_ID,
            application = SPARK_APP_JAR,
            java_class = "com.crm_erp_analytics.gold.DimProductJob"
        )
        fact_sales = SparkSubmitOperator(
            task_id = "fact_sales",
            conn_id = SPARK_CONN_ID,
            application = SPARK_APP_JAR,
            java_class = "com.crm_erp_analytics.gold.FactSalesJob"
        )
        validate_data = PythonOperator(
            task_id="validate_data",
            python_callable=validate_gold_tables,
        )
        create_power_bi_views_task = PythonOperator(
            task_id="create_power_bi_views",
            python_callable=create_power_bi_views,
        )
        create_gold_schema_task >> dim_customer
        create_gold_schema_task >> dim_product
        dim_customer >> fact_sales 
        dim_product >> fact_sales 
        fact_sales >> validate_data >> create_power_bi_views_task
    end = EmptyOperator(task_id = "end")

start >> extract_group >> transform_group >> load_dwh_group>> end
