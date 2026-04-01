package com.crm_erp_analytics.silver.erp

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils
import org.apache.spark.sql.expressions.Window

object ProductCategoriesCleanJob{

    def extract(spark: SparkSession): DataFrame = {
        IOUtils.readCsv(spark,Config.Bronze.productCategories)
    }
    def transform(df:DataFrame): DataFrame = {
        val df_clean_cat = df.withColumn("CAT",trim(col("CAT")))
        val df_clean_subcat = df_clean_cat.withColumn("SUBCAT",trim(col("SUBCAT")))
        val df_clean_maintenance = df_clean_subcat.withColumn("MAINTENANCE",trim(col("MAINTENANCE")))
        val df_clean = DataFrameUtils.addTechnicalColumns(df_clean_maintenance)
        df_clean
    }
    def load(df: DataFrame): Unit = {
        IOUtils.writeParquet(df,Config.Silver.productCategoriesClean)
    }
    def main(args: Array[String]): Unit = {
        val spark = SparkSessionFactory.create("ProductCategoriesCleanJob")
        val df = extract(spark)
        val df_clean = transform(df)
        load(df_clean)
    }









}