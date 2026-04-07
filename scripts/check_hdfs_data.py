from hdfs import InsecureClient


def check_hdfs_data(hdfs_path, number_files):
    try:
        print(f"\n checking hdfs data in {hdfs_path}.... ")
        number_files = int(number_files)
        client = InsecureClient(url="http://namenode:9870", user="root")
        entries = client.list(hdfs_path, status=True)
        csv_files = [
            name for name, status in entries
            if status["type"] == "FILE" and name.endswith(".csv")
        ]
        ignored_entries = [
            name for name, status in entries
            if status["type"] != "FILE" or not name.endswith(".csv")
        ]

        if ignored_entries:
            print(f"ignoring non-data HDFS entries in {hdfs_path}: {ignored_entries}")

        if len(csv_files) != number_files:
            raise ValueError(
                f"CSV files in {hdfs_path}: expected {number_files}, found {len(csv_files)}. "
                f"files={csv_files}"
            )
        print(f"all files in {hdfs_path} checked successfully")
    except Exception as e:
        print(f"check hdfs data failed: {e}")
        raise e
