CREATE SCHEMA IF NOT EXISTS bi;

-- Power BI detail view for sales analysis
CREATE OR REPLACE VIEW bi.vw_sales_analysis AS
SELECT
    fs.order_number,
    fs.order_date,
    fs.ship_date,
    fs.due_date,
    fs.sales_amount,
    fs.quantity,
    fs.unit_price,

    dc.customer_key,
    dc.customer_id,
    dc.customer_number,
    dc.first_name,
    dc.last_name,
    dc.country,
    dc.marital_status,
    dc.gender,
    dc.birthdate,
    dc.create_date AS customer_create_date,

    dp.product_key,
    dp.product_id,
    dp.product_number,
    dp.product_name,
    dp.category_id,
    dp.category,
    dp.subcategory,
    dp.maintenance,
    dp.cost,
    dp.product_line,
    dp.start_date AS product_start_date

FROM gold.fact_sales fs
LEFT JOIN gold.dim_customer dc
    ON fs.customer_key = dc.customer_key
LEFT JOIN gold.dim_product dp
    ON fs.product_key = dp.product_key;

-- Power BI aggregated view for customer sales analysis

CREATE OR REPLACE VIEW bi.vw_customer_sales AS
SELECT
    dc.customer_key,
    dc.customer_id,
    dc.customer_number,
    dc.first_name,
    dc.last_name,
    dc.country,
    dc.marital_status,
    dc.gender,
    dc.birthdate,
    COUNT(fs.order_number) AS total_orders,
    COALESCE(SUM(fs.sales_amount), 0) AS total_sales,
    COALESCE(SUM(fs.quantity), 0) AS total_quantity,
    AVG(fs.unit_price) AS avg_unit_price
FROM gold.dim_customer dc
LEFT JOIN gold.fact_sales fs
    ON dc.customer_key = fs.customer_key
GROUP BY
    dc.customer_key,
    dc.customer_id,
    dc.customer_number,
    dc.first_name,
    dc.last_name,
    dc.country,
    dc.marital_status,
    dc.gender,
    dc.birthdate;

-- Power BI aggregated view for product sales analysis

CREATE OR REPLACE VIEW bi.vw_product_sales AS
SELECT
    dp.product_key,
    dp.product_id,
    dp.product_number,
    dp.product_name,
    dp.category_id,
    dp.category,
    dp.subcategory,
    dp.maintenance,
    dp.cost,
    dp.product_line,
    COUNT(fs.order_number) AS total_orders,
    COALESCE(SUM(fs.sales_amount), 0) AS total_sales,
    COALESCE(SUM(fs.quantity), 0) AS total_quantity,
    AVG(fs.unit_price) AS avg_unit_price
FROM gold.dim_product dp
LEFT JOIN gold.fact_sales fs
    ON dp.product_key = fs.product_key
GROUP BY
    dp.product_key,
    dp.product_id,
    dp.product_number,
    dp.product_name,
    dp.category_id,
    dp.category,
    dp.subcategory,
    dp.maintenance,
    dp.cost,
    dp.product_line;

-- Power BI aggregated view for monthly sales analysis

CREATE OR REPLACE VIEW bi.vw_monthly_sales AS
SELECT
    DATE_TRUNC('month', fs.order_date)::date AS sales_month,
    COUNT(fs.order_number) AS total_orders,
    COALESCE(SUM(fs.sales_amount), 0) AS total_sales,
    COALESCE(SUM(fs.quantity), 0) AS total_quantity,
    AVG(fs.unit_price) AS avg_unit_price
FROM gold.fact_sales fs
GROUP BY DATE_TRUNC('month', fs.order_date)::date
ORDER BY sales_month;
