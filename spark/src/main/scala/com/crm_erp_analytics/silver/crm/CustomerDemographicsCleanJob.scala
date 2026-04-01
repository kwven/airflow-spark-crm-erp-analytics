package com.crm_erp_analytics.silver.crm

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

object CustomerDemographicsCleanJob{

    def extract(spark: SparkSession): DataFrame = {
        IOUtils.readCsv(spark,Config.Bronze.customerDemographics)
    }
    def transform(df:DataFrame): DataFrame = {
        val df_clean_cid = df.withColumn("CID",when(col("CID") like "NAS%",substring(col("CID"),4,1000)).otherwise(col("CID")))
        val df_clean_bdate = df_clean_cid.withColumn("BDATE",to_date(col("BDATE"),"yyyy-MM-dd"))
        .withColumn("BDATE",when(col("BDATE") >= current_date(),null).otherwise(col("BDATE")))
        val df_clean = df_clean_bdate.withColumn("GEN",when(upper(trim(col("GEN"))).isin("F","FEMALE"),lit("Female")).when(upper(trim(col("GEN"))).isin("M","MALE"),lit("Male")).otherwise("Unknown"))
        df_clean
    }
    def load(df: DataFrame): Unit = {
        IOUtils.writeParquet(df,Config.Silver.customerDemographicsClean)
    }
    def main(args: Array[String]): Unit = {
        val spark = SparkSessionFactory.create("CustomerDemographicsCleanJob")
        val dfDemographics = extract(spark)
        val dfClean = transform(dfDemographics)
        load(dfClean)
        spark.stop()


    }









}