package com.crm_erp_analytics.gold

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

object FactSalesJob {  
  def extract(spark: SparkSession): (DataFrame,DataFrame,DataFrame)  = {
    val df1 = IOUtils.readParquet(spark,Config.Silver.salesTransactionsClean)
    val df2 = IOUtils.readJdbcTable(spark,Config.Gold.dimProduct)
    val df3 = IOUtils.readJdbcTable(spark,Config.Gold.dimCustomer)
    (df1,df2,df3)
  }
  def transform(df1: DataFrame, df2: DataFrame,df3: DataFrame): DataFrame = {
    val df_join = df1.join(df2,df1.col("sls_prd_key") === df2.col("product_number"),"left").join(df3,df1.col("sls_cust_id") === df3.col("customer_id"),"left")
    val df_clean_name = df_join.withColumnRenamed("sls_ord_num","order_number")
                               .withColumnRenamed("sls_order_dt","order_date")
                               .withColumnRenamed("sls_ship_dt","ship_date")
                               .withColumnRenamed("sls_due_dt","due_date")
    val df_clean = df_clean_name.select(
      col("order_number").cast("string").as("order_number"),
      col("product_key").cast("long").as("product_key"),
      col("customer_key").cast("long").as("customer_key"),
      col("order_date").cast("date").as("order_date"),
      col("ship_date").cast("date").as("ship_date"),
      col("due_date").cast("date").as("due_date"),
      col("sls_sales").cast("decimal(18,2)").as("sales_amount"),
      col("sls_quantity").cast("int").as("quantity"),
      col("sls_price").cast("decimal(18,2)").as("unit_price")
    )
    df_clean
  }
  def load(df: DataFrame): Unit = {
    IOUtils.writeJdbcTable(df,Config.Gold.factSales)
  }
  def main(args: Array[String]): Unit = {
    val spark = SparkSessionFactory.create("FactSalesJob")
    val (df1,df2,df3) = extract(spark)
    val df_clean= transform(df1,df2,df3)
    load(df_clean)
    spark.stop()
  }
}
