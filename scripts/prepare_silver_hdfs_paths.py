from hdfs import InsecureClient


def prepare_silver_hdfs_paths() -> None:
    """Ensure Spark output roots exist and are writable by non-root users."""
    client = InsecureClient(url="http://namenode:9870", user="root")
    output_roots = ["/data/silver", "/data/reject"]

    for hdfs_path in output_roots:
        client.makedirs(hdfs_path)
        # Spark jobs run as `airflow`; keep directory writable to avoid HDFS ACL failures.
        client.set_permission(hdfs_path, permission="777")
        print(f"Prepared {hdfs_path} with permission 777")
