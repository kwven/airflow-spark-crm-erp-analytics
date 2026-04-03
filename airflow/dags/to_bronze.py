from airflow import DAG
from airflow.providers.standard.operators.python import PythonOperator
from datetime import datetime
from airflow.providers.standard.operators.empty import EmptyOperator

## functions to ingest and check data imported to hdfs 
from scripts.ingest_to_hdfs import ingest_to_hdfs
from scripts.check_hdfs_data import check_hdfs_data

with DAG (
    dag_id = "to_bronze",
    schedule = "0 0 * * *",
    start_date = datetime(2026,4,1),
    catchup = False,
   ) as dag:
    
    start = EmptyOperator(task_id="start", dag=dag)

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
    
    end = EmptyOperator(task_id="end", dag=dag)
    start >> ingest_crm_task >> check_crm_task >> end
    start >> ingest_erp_task >> check_erp_task >> end
