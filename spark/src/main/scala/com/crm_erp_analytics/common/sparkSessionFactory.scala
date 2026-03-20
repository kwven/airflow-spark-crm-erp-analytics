package com.crm_erp_analytics.common

import org.apache.spark.sql.SparkSession

object SparkSessionFactory{
    def create(appName: String): SparkSession = {
        SparkSession
        .builder()
        .appName(appName)
        .config("spark.sql.session.timeZone", "UTC")
        .config("spark.sql.sources.partitionOverwriteMode", "dynamic")
        .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
        .getOrCreate()
    }
}