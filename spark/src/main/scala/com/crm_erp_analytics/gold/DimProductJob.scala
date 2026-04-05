package com.crm_erp_analytics.gold

import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import com.crm_erp_analytics.common.Config
import com.crm_erp_analytics.common.SparkSessionFactory
import com.crm_erp_analytics.common.IOUtils
import com.crm_erp_analytics.common.DataFrameUtils

object DimProductJob {  
  def extract(spark: SparkSession): (DataFrame,DataFrame)  = {
    val df1 = IOUtils.readParquet(spark,Config.Silver.productMasterClean)
    val df2 = IOUtils.readParquet(spark,Config.Silver.productCategoriesClean)
    (df1,df2)
  }
  def transform(df1: DataFrame, df2: DataFrame): DataFrame = {
    val df_noHistory = df1.filter(col("prd_end_dt").isNull) 
    val df_join = df_noHistory.join(df2,df_noHistory.col("cat_id") === df2.col("ID"),"left")
    val df_clean_named = df_join.withColumnRenamed("prd_id","product_id")
      .withColumnRenamed("prd_key","product_number")
      .withColumnRenamed("prd_nm","product_name")
      .withColumnRenamed("cat_id","category_id")
      .withColumnRenamed("CAT","category")
      .withColumnRenamed("SUBCAT","subcategory")
      .withColumnRenamed("prd_cost","cost")
      .withColumnRenamed("prd_line","product_line")
      .withColumnRenamed("prd_start_dt","start_date")
    val windowkey = Window.orderBy("product_number","start_date")
    val df_clean_surrogate = df_clean_named.withColumn("product_key",row_number().over(windowkey))
    val df_clean = df_clean_surrogate.select("product_key","product_id","product_number","product_name","category_id","category","subcategory","maintenance","cost","product_line","start_date")
    df_clean
  }
  def load(df: DataFrame): Unit = {
    IOUtils.writeJdbcTable(df, "gold.dim_product", "overwrite")
  }
  def main(args: Array[String]): Unit = {
    val spark = SparkSessionFactory.create("DimProductJob")
    val (dfProductMaster, dfProductCategories) = extract(spark)
    val dfClean = transform(dfProductMaster, dfProductCategories)
    load(dfClean)
    spark.stop()
  }
}
