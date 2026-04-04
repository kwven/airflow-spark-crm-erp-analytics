package com.crm_erp_analytics.common

import org.apache.spark.sql.SparkSession

object SparkSessionFactory{
    def create(appName: String): SparkSession = {
        val builder = SparkSession
          .builder()
          .appName(appName)
          .config("spark.sql.session.timeZone", "UTC")
          .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
          .config("spark.sql.legacy.timeParserPolicy", "LEGACY")

        Config.Spark.master.foreach(builder.master)

        builder.getOrCreate()
    }
}