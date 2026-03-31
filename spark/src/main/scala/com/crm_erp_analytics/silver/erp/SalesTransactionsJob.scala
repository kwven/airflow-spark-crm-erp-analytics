package com.crm_erp_analytics.silver.erp

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

object SalesTransactionsJob {

    def extract(spark: SparkSession): DataFrame = {
      IOUtils.readCsv(spark,Config.Bronze.salesTransactions)
    }
    def transform(df: DataFrame): DataFrame = {
        // cleaning and casting date s as dates 
        val df_clean_dates = df.withColumn("sls_order_dt",when(col("sls_order_dt") === 0,null).when(len(col("sls_order_dt")) =!= 8,null).otherwise(to_date(col("sls_order_dt"),"yyyyMMdd")))
        .withColumn("sls_ship_dt",when(col("sls_ship_dt") === 0,null).when(len(col("sls_ship_dt")) =!= 8,null).otherwise(to_date(col("sls_ship_dt"),"yyyyMMdd")))
        .withColumn("sls_due_dt",when(col("sls_due_dt") === 0,null).when(len(col("sls_due_dt")) =!= 8,null).otherwise(to_date(col("sls_due_dt"),"yyyyMMdd")))
        // cleaning slaes and price columns 
        val df_clean_sales = df_clean_dates.withColumn("sls_sales",when(col("sls_sales") <= 0 || col("sls_sales").isNull || col("sls_sales") =!= abs(col("sls_price").cast("double")) * col("sls_quantity"),abs(col("sls_price").cast("double")) * col("sls_quantity")).otherwise(col("sls_sales").cast("double")))
        val df_clean_price = df_clean_sales.withColumn("sls_price",when(col("sls_price") <= 0 || col("sls_price").isNull ,abs(col("sls_sales").cast("double")) / col("sls_quantity")).otherwise(col("sls_price").cast("double")))
      df_clean_price
    }
    def load(df: DataFrame): Unit = {
      IOUtils.writeParquet(df,Config.Silver.salesTransactionsClean)
    }
    def main(args: Array[String]): Unit = {
      val spark = SparkSessionFactory.create("SalesTransactionsJob")
      val df = extract(spark)
      val df_clean = transform(df)
      load(df_clean)
}
}