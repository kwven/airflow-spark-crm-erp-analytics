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

  def readJdbcTable(spark: SparkSession, table: String): DataFrame = {
    spark.read
      .format("jdbc")
      .option("url", Config.Jdbc.url)
      .option("dbtable", table)
      .option("user", Config.Jdbc.user)
      .option("password", Config.Jdbc.password)
      .option("driver", Config.Jdbc.driver)
      .load()
  }

  def readJdbcQuery(spark: SparkSession, query: String, alias: String = "src"): DataFrame = {
    spark.read
      .format("jdbc")
      .option("url", Config.Jdbc.url)
      .option("dbtable", s"(${query}) ${alias}")
      .option("user", Config.Jdbc.user)
      .option("password", Config.Jdbc.password)
      .option("driver", Config.Jdbc.driver)
      .load()
  }

  def writeJdbcTable(df: DataFrame, table: String, mode: String = "append"): Unit = {
    df.write
      .format("jdbc")
      .option("url", Config.Jdbc.url)
      .option("dbtable", table)
      .option("user", Config.Jdbc.user)
      .option("password", Config.Jdbc.password)
      .option("driver", Config.Jdbc.driver)
      .mode(mode)
      .save()
  }
}
