package com.crm_erp_analytics.crm

import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

//---------------
// this file contains the logic to clean the customer master data 
// -1 we extract the file 
// -2 we start by 
//---------------

object CustomerMasterClean{
    def extract(Spark: SparkSession): DataFrame ={
        IOUtils.readCsv(Spark,Config.Bronze.CustomerMaster)
    }

    def transform(df:DataFrame): DataFrame ={
    

    }
    def load(df:DataFrame): Unit ={


    }
    def main(args: Array[String]): Unit ={
        val Spark = SparkSessionFactory.create("CustomerMasterCleanjob")
    }
}