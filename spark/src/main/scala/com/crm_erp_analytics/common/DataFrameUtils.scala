package com.crm_erp_analytics.common

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.StringType

object DataFrameUtils {
    def addTechnicalColumns(df: DataFrame): DataFrame ={
        df.withColumn("data_create_date", current_timestamp())
        }
}

