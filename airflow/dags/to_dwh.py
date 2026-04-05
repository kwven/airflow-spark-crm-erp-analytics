from airflow import DAG
from datetime import datetime
import os

from airflow.providers.standard.operators.empty import EmptyOperator
from airflow.providers.standard.operators.bash import BashOperator
from airflow.providers.standard.operators.python import PythonOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator

from scripts.prepare_silver_hdfs_paths import prepare_silver_hdfs_paths


SPARK_CONN_ID = "spark_default"
SPARK_APP_JAR = os.getenv(
    "SPARK_APP_JAR",
    "/opt/airflow/spark/target/scala-2.12/crm-erp-analytic-spark_2.12-0.1.0.jar",
)

with DAG(
    dag_id = "to_dwh",
    schedule = "0 0 * * *",
    start_date = datetime(2026,4,1),
    catchup = False
) as dag:

    start = EmptyOperator(task_id = "start")

    compile_jar = BashOperator(
        task_id = "compile_jar",
        bash_command = "cd /opt/airflow/spark && sbt clean assembly"
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

    end = EmptyOperator(task_id = "end")

    start >> compile_jar >> dim_customer >> dim_product >> fact_sales >> end
