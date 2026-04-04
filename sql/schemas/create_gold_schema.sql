CREATE SCHEMA IF NOT EXISTS gold;
CREATE SCHEMA IF NOT EXISTS bi;

CREATE TABLE IF NOT EXISTS gold.dim_customer (
  customer_key BIGINT PRIMARY KEY,
  customer_id BIGINT,
  customer_number TEXT,
  first_name TEXT,
  last_name TEXT,
  country TEXT,
  marital_status TEXT,
  gender TEXT,
  birthdate DATE,
  create_date DATE
);

CREATE TABLE IF NOT EXISTS gold.dim_product (
  product_key BIGINT PRIMARY KEY,
  product_id BIGINT,
  product_number TEXT,
  product_name TEXT,
  category_id TEXT,
  category TEXT,
  subcategory TEXT,
  maintenance BOOLEAN,
  cost NUMERIC(18, 2),
  product_line TEXT,
  start_date DATE
);

CREATE TABLE IF NOT EXISTS gold.fact_sales (
  order_number TEXT,
  product_key BIGINT,
  customer_key BIGINT,
  order_date DATE,
  ship_date DATE,
  due_date DATE,
  sales_amount NUMERIC(18, 2),
  quantity INTEGER,
  unit_price NUMERIC(18, 2)
);
