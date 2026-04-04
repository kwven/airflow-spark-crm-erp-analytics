from airflow import DAG
from datetime import datetime

from airflow.providers.standard.operators.empty import EmptyOperator
from airflow.providers.standard.operators.bash import BashOperator
from airflow.providers.standard.operators.python import PythonOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from scripts.prepare_silver_hdfs_paths import prepare_silver_hdfs_paths


with DAG(
    dag_id = "to_silver",
    schedule = "0 0 * * *",
    start_date = datetime(2026,4,1),
    catchup = False,
)as dag:
    
    start = EmptyOperator(task_id="start", dag=dag)
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

    end = EmptyOperator(task_id="end", dag=dag)
    start >> prepare_hdfs_output_paths >> compile_jar \
    >> transform_crm_demographics \
    >> transform_crm_locations \
    >> transform_crm_master \
    >> transform_erp_categories \
    >> transform_erp_transactions \
    >> transform_erp_master \
    >> end
