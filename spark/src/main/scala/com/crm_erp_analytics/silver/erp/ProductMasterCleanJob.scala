package com.crm_erp_analytics.silver.erp

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils
import org.apache.spark.sql.expressions.Window

object ProductMasterCleanJob{

    def extract(spark: SparkSession): DataFrame = {
        IOUtils.readCsv(spark,Config.Bronze.productMaster)
    }
    def transform(df:DataFrame): DataFrame = {
        val df_cat = df.withColumn("cat_id",substring(col("prd_key"),1,5))
        .withColumn("prd_key",substring(col("prd_key"),7,10000))
        val df_trimmed = df_cat.withColumn("prd_nm",trim(col("prd_nm")))
        val df_cost = df_trimmed.withColumn("prd_cost",when(col("prd_cost").isNull,lit(0.0)).otherwise(col("prd_cost").cast("double")))
        val df_line = df_cost.withColumn("prd_line",when(upper(trim(col("prd_line"))) === "M", "Mountain").when(upper(trim(col("prd_line"))) === "R", "Road").when(upper(trim(col("prd_line"))) === "S", "other Sales").when(upper(trim(col("prd_line"))) === "T", "Touring").otherwise("Unkown"))
        val df_with_date = df_line.withColumn("prd_end_dt",to_date(col("prd_end_dt"),"yyyy-MM-dd"))     
        val df_with_date_start = df_with_date.withColumn("prd_start_dt",to_date(col("prd_start_dt"),"yyyy-MM-dd"))
        // using a lead window function to change end_date to the first_date of nexte profuct  minus 1 day 
        val window_lead = Window.partitionBy("prd_key").orderBy(col("prd_start_dt"))
        val df_date_clean = df_with_date_start.withColumn("prd_end_dt",date_sub(lead(col("prd_start_dt"),1).over(window_lead),1))
        val df_clean = DataFrameUtils.addTechnicalColumns(df_date_clean)
        df_clean
    }
    def load(df: DataFrame): Unit = {
        IOUtils.writeParquet(df,Config.Silver.productMasterClean)
    }
    def main(args: Array[String]): Unit = {
        val spark = SparkSessionFactory.create("ProductMasterCleanJob")
        val df = extract(spark)
        val df_clean = transform(df)
        load(df_clean)
    }









}