package com.crm_erp_analytics.common

import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}

object Config {

  private val conf: TypesafeConfig = ConfigFactory.load()

  object Hdfs {
    val baseUri: String = conf.getString("app.hdfs.base-uri")
  }

  object Paths {
    val bronzeBase: String = conf.getString("app.paths.bronze-base")
    val silverBase: String = conf.getString("app.paths.silver-base")
    val rejectBase: String = conf.getString("app.paths.reject-base")
  }

  object Bronze {
    val customerMaster: String =
      s"${Paths.bronzeBase}/crm/crm_customer_master.csv"

    val customerDemographics: String =
      s"${Paths.bronzeBase}/crm/crm_customer_demographics.csv"

    val customerLocations: String =
      s"${Paths.bronzeBase}/crm/crm_customer_locations.csv"

    val productMaster: String =
      s"${Paths.bronzeBase}/erp/erp_product_master.csv"

    val productCategories: String =
      s"${Paths.bronzeBase}/erp/erp_product_categories.csv"

    val salesTransactions: String =
      s"${Paths.bronzeBase}/erp/erp_sales_transactions.csv"
  }

  object Silver {
    val customerMasterClean: String =
      s"${Paths.silverBase}/crm/crm_customer_master_clean"

    val customerDemographicsClean: String =
      s"${Paths.silverBase}/crm/crm_customer_demographics_clean"

    val customerLocationsClean: String =
      s"${Paths.silverBase}/crm/crm_customer_locations_clean"

    val productMasterClean: String =
      s"${Paths.silverBase}/erp/erp_product_master_clean"

    val productCategoriesClean: String =
      s"${Paths.silverBase}/erp/erp_product_categories_clean"

    val salesTransactionsClean: String =
      s"${Paths.silverBase}/erp/erp_sales_transactions_clean"
  }

  object Rejects {
    val salesTransactionsReject: String =
      s"${Paths.rejectBase}/sales_transactions_reject"
  }

  object ReadOptions {
    val header: Boolean = conf.getBoolean("app.read.header")
    val inferSchema: Boolean = conf.getBoolean("app.read.infer-schema")
    val delimiter: String = conf.getString("app.read.delimiter")
  }

  object WriteOptions {
    val mode: String = conf.getString("app.write.mode")
    val format: String = conf.getString("app.write.format")
  }

  object Jdbc {
    val url: String = conf.getString("app.jdbc.url")
    val user: String = conf.getString("app.jdbc.user")
    val password: String = conf.getString("app.jdbc.password")
    val driver: String = conf.getString("app.jdbc.driver")
  }
}