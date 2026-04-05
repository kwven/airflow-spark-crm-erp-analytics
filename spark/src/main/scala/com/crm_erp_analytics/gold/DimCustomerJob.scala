package com.crm_erp_analytics.gold

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

object DimCustomerJob {
  def extract(spark: SparkSession): (DataFrame,DataFrame,DataFrame)  = {
    val df1 = IOUtils.readParquet(spark,Config.Silver.customerMasterClean)
    val df2 = IOUtils.readParquet(spark,Config.Silver.customerLocationsClean)
    val df3 = IOUtils.readParquet(spark,Config.Silver.customerDemographicsClean)
    (df1,df2,df3)
  }
  def transform(df1: DataFrame, df2: DataFrame, df3: DataFrame): DataFrame = {
    val df_join = df1.join(df2,df1.col("cst_key") === df2.col("CID"),"left").join(df3,df1.col("cst_key") === df3.col("CID"),"left")
    val df_clean_gen = df_join.withColumn("cst_gndr",when(col("cst_gndr") === "Unknown",coalesce(col("GEN"),lit("Unknown"))).otherwise(col("cst_gndr")))
    val df_named =df_clean_gen.withColumnRenamed("cst_key","customer_number")
      .withColumnRenamed("cst_id","customer_id")
      .withColumnRenamed("cst_firstname","first_name")
      .withColumnRenamed("cst_lastname","last_name")
      .withColumnRenamed("cst_marital_status","marital_status")
      .withColumnRenamed("cst_gndr","gender")
      .withColumnRenamed("cst_create_date","create_date")
      .withColumnRenamed("CNTRY","country")
      .withColumnRenamed("BDATE","birthdate")
    val windowkey = Window.orderBy("customer_id")
    val df_clean_key = df_named.withColumn("customer_key",row_number().over(windowkey))
    val df_clean = df_clean_key.select("customer_key","customer_number","customer_id","first_name","last_name","country","marital_status","gender","birthdate","create_date")
    df_clean
  }
  def load(df: DataFrame): Unit = {
    IOUtils.writeJdbcTable(df, "gold.dim_customer", "overwrite")
  }
  def main(args: Array[String]): Unit = {
    val spark = SparkSessionFactory.create("DimCustomerJob")
    val (dfCustomerMaster, dfCustomerLocations, dfCustomerDemographics) = extract(spark)
    val dfClean = transform(dfCustomerMaster, dfCustomerLocations, dfCustomerDemographics)
    load(dfClean)
    spark.stop()
  }
}
