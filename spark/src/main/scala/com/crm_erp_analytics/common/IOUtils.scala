package com.crm_erp_analytics.common

import org.apache.spark.sql.{DataFrame, SparkSession}

object IOUtils {

  def readCsv(spark: SparkSession, path: String): DataFrame = {
    spark.read
      .option("header", Config.ReadOptions.header.toString)
      .option("inferSchema", Config.ReadOptions.inferSchema.toString)
      .option("delimiter", Config.ReadOptions.delimiter)
      .csv(path)
  }

  def readParquet(spark: SparkSession, path: String): DataFrame = {
    spark.read.parquet(path)
  }

  def writeParquet(df: DataFrame, path: String): Unit = {
    df.write
      .mode(Config.WriteOptions.mode)
      .format(Config.WriteOptions.format)
      .save(path)
  }

  def writeReject(df: DataFrame, path: String): Unit = {
    df.write
      .mode(Config.WriteOptions.mode)
      .format(Config.WriteOptions.format)
      .save(path)
  }
}