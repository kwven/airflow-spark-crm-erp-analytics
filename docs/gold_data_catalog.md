# Gold Layer Data Catalog

This catalog documents the Gold layer tables in schema `gold` for this project.
Column types are taken from `sql/schemas/create_gold_schema.sql`, and descriptions are aligned with the Spark jobs in `spark/src/main/scala/com/crm_erp_analytics/gold`.

## `gold.dim_customer`

Business purpose: customer dimension used for sales analysis and customer slicing.

| Column | Type | Description |
|---|---|---|
| `customer_key` | `BIGINT` | Surrogate key generated in Gold (`row_number` over `customer_id`), primary key of the dimension. |
| `customer_id` | `BIGINT` | Customer identifier from CRM master data (`cst_id`), used to link transactional customer IDs to the dimension. |
| `customer_number` | `TEXT` | Customer business/reference number from CRM (`cst_key`). |
| `first_name` | `TEXT` | Customer first name (`cst_firstname`) after trimming. |
| `last_name` | `TEXT` | Customer last name (`cst_lastname`) after trimming. |
| `country` | `TEXT` | Customer country from CRM locations (`CNTRY`) after country normalization. |
| `marital_status` | `TEXT` | Standardized marital status from CRM master (`Single`, `Married`, `Unknown`). |
| `gender` | `TEXT` | Standardized gender. Uses CRM master value, and falls back to demographics (`GEN`) when CRM value is `Unknown`. |
| `birthdate` | `DATE` | Customer birth date from demographics (`BDATE`) after date validation. |
| `create_date` | `DATE` | Record creation date from CRM master (`cst_create_date`). |

## `gold.dim_product`

Business purpose: product dimension for product/category reporting and product-level sales analysis.

| Column | Type | Description |
|---|---|---|
| `product_key` | `BIGINT` | Surrogate key generated in Gold (`row_number` over `product_number`, `start_date`), primary key of the dimension. |
| `product_id` | `BIGINT` | Product identifier from ERP product master (`prd_id`). |
| `product_number` | `TEXT` | Product business/reference number from ERP (`prd_key`, cleaned in Silver). |
| `product_name` | `TEXT` | Product name from ERP (`prd_nm`) after trimming. |
| `category_id` | `TEXT` | Product category identifier (`cat_id`) extracted/cleaned from ERP product key. |
| `category` | `TEXT` | Product category name from product categories (`CAT`). |
| `subcategory` | `TEXT` | Product subcategory name from product categories (`SUBCAT`). |
| `maintenance` | `BOOLEAN` | Maintenance flag from product categories (`MAINTENANCE`) indicating whether the product requires maintenance. |
| `cost` | `NUMERIC(18, 2)` | Product cost from ERP (`prd_cost`), cleaned and cast to numeric. |
| `product_line` | `TEXT` | Standardized product line (`Mountain`, `Road`, `Touring`, etc.) from ERP (`prd_line`). |
| `start_date` | `DATE` | Product start/valid-from date from ERP (`prd_start_dt`). |

## `gold.fact_sales`

Business purpose: sales fact table at order line level, linked to customer and product dimensions.

| Column | Type | Description |
|---|---|---|
| `order_number` | `TEXT` | Sales order number from ERP transactions (`sls_ord_num`). |
| `product_key` | `BIGINT` | Foreign key to `gold.dim_product.product_key`, resolved by matching `sls_prd_key` to `dim_product.product_number`. |
| `customer_key` | `BIGINT` | Foreign key to `gold.dim_customer.customer_key`, resolved by matching `sls_cust_id` to `dim_customer.customer_id`. |
| `order_date` | `DATE` | Order date from sales transactions (`sls_order_dt`) after parsing/validation. |
| `ship_date` | `DATE` | Ship date from sales transactions (`sls_ship_dt`) after parsing/validation. |
| `due_date` | `DATE` | Due date from sales transactions (`sls_due_dt`) after parsing/validation. |
| `sales_amount` | `NUMERIC(18, 2)` | Total line sales amount from `sls_sales` after data-quality correction in Silver and cast in Gold. |
| `quantity` | `INTEGER` | Sold quantity from `sls_quantity`. |
| `unit_price` | `NUMERIC(18, 2)` | Unit price from `sls_price` after data-quality correction in Silver and cast in Gold. |

## Notes

1. `dim_customer` and `dim_product` are currently written with Spark JDBC `overwrite` mode in Gold jobs.
2. `fact_sales` is currently written with Spark JDBC `append` mode in Gold job helper default.
