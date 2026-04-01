package com.crm_erp_analytics.silver.crm

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

object CustomerLocationsCleanJob{

    def extract(spark: SparkSession): DataFrame = {
        IOUtils.readCsv(spark,Config.Bronze.customerLocations)
    }
    def transform(df:DataFrame): DataFrame = {
        val df_clean_cid = df.withColumn("CID",regexp_replace(col("CID"),"-",""))
        val df_clean = df_clean_cid.withColumn("CNTRY",when(upper(trim(col("CNTRY"))).isin("US","USA"),"United States").when(upper(trim(col("CNTRY"))) === "DE","Germany").when(upper(trim(col("CNTRY"))) === "" || col("CNTRY").isNull,"Unkown").otherwise(trim(col("CNTRY"))))
        df_clean
    }
    def load(df: DataFrame): Unit = {
        IOUtils.writeParquet(df,Config.Silver.customerLocationsClean)
    }
    def main(args: Array[String]): Unit = {
        val spark = SparkSessionFactory.create("CustomerLocationsCleanJob")
        val df = extract(spark)
        val dfClean = transform(df)
        load(dfClean)
        spark.stop()
    }
}