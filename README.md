# Airflow Spark CRM ERP Analytics

> End-to-end data engineering project that turns raw CRM and ERP files into business-ready data and Power BI dashboards.

## Project Snapshot

- **Sources:** CRM + ERP CSV files  
- **Orchestration:** Apache Airflow  
- **Processing:** Apache Spark + Scala  
- **Storage:** HDFS + PostgreSQL  
- **Analytics:** Power BI

## What I Built

- Ingested raw CRM and ERP data into a **bronze** layer
- Cleaned and standardized data in a **silver** layer
- Built a **gold** layer with a star schema for analytics
- Published business-ready views for **Power BI**

## Architecture

![Architecture](docs/architecture.png)


## End-to-End Data Pipeline in Airflow

![Airflow DAG](docs/first_part_dag.png)

![Airflow DAG](docs/seconde_part_dag.png)

## Dashboards

### Sales Overview
![Sales Overview Dashboard](docs/dashboard-sales-overview.png)

### Customer Analysis
![Customer Analysis Dashboard](docs/dashboard-customer-analysis.png)

### Product Analysis
![Product Analysis Dashboard](docs/dashboard-product-analysis.png)

## Tech Stack

`Docker` `Apache Airflow` `Apache Spark` `Scala` `Python` `Hadoop HDFS` `PostgreSQL` `Power BI`

## Pipeline Flow

| Layer | Goal |
|---|---|
| **Bronze** | Store raw batch data |
| **Silver** | Clean, standardize, and enrich data |
| **Gold** | Build analytics tables and reporting views |

## Repo Highlights

- **Airflow DAGs** orchestrate the ETL flow
- **Spark jobs** process and model the data
- **PostgreSQL** stores the gold warehouse
- **Power BI** consumes the final model for dashboards

## Connect with Me
<div align="center">
  <a href="https://www.linkedin.com/in/ahmedlamrani/">
    <img src="https://img.shields.io/badge/LinkedIn-%230077B5.svg?&style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" style="margin-bottom: 5px;"/>
  </a>
  <a href="https://github.com/kwven">
    <img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" style="margin-bottom: 5px;"/>
  </a>
</div>
