package com.crm_erp_analytics.silver.crm
import org.apache.spark.sql.functions.{col,row_number,to_timestamp,trim,upper,when}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils
import javax.xml.crypto.Data

//---------------
// this file contains the logic to clean the customer master data 
// Data cleansing
// -1 we extract the file 
// -2 we start by creating a tag for duplicated id of sustomer and take only the newest ones 
// -3 we trim the firstname and lastname
// -4 we replace the abreviation name in database M --> Male and F --> Female
// -5 we add the technical columns
// Load the cleaned data to the silver layer
//---------------

object CustomerMasterClean{
    def extract(spark: SparkSession): DataFrame ={
        IOUtils.readCsv(spark,Config.Bronze.customerMaster)
    }
    def transform(df: DataFrame): DataFrame ={
        // start by creating a tag for duplicated id of sustomer and take only the newest ones 
        val windowpar = Window.partitionBy(col("cst_id")).orderBy(col("cst_create_date").desc)
        val df_with_date = df.withColumn("cst_create_date",to_timestamp(col("cst_create_date"),"yyyy-MM-dd"))
        val df_tag = df_with_date.withColumn("tag",row_number().over(windowpar))
        val df_nodup = df_tag.filter(col("tag") === 1)
        // trim the firstname and lastname
        val df_clean = df_nodup
        .withColumn("cst_firstname",trim(col("cst_firstname")))
        .withColumn("cst_lastname",trim(col("cst_lastname")))
        // no abreviation name in database M --> Male and F --> Female
        .withColumn("cst_gndr",when(upper(trim(col("cst_gndr"))) === "M","Male").when(upper(trim(col("cst_gndr"))) === "F","Female").otherwise("Unknown"))
        .withColumn("cst_marital_status",when(upper(trim(col("cst_marital_status"))) === "S","Single").when(upper(trim(col("cst_marital_status"))) === "M","Married").otherwise("Unknown"))
        //add technical culumns 
        val df_final = DataFrameUtils.addTechnicalColumns(df_clean)
        df_final
    }

    def load(df: DataFrame): Unit ={
        IOUtils.writeParquet(df,Config.Silver.customerMasterClean)

    }
    def main(args: Array[String]): Unit ={
        val spark = SparkSessionFactory.create("CustomerMasterCleanJob")
        val df = extract(spark)
        val df_clean = transform(df)
        load(df_clean)
    }
}